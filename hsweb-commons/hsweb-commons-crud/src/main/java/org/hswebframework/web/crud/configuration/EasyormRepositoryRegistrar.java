package org.hswebframework.web.crud.configuration;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.mapping.defaults.DefaultReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.DefaultSyncRepository;
import org.hswebframework.utils.ClassUtils;
import org.hswebframework.web.crud.annotation.EnableEasyormRepository;
import org.hswebframework.web.api.crud.entity.ImplementFor;
import org.hswebframework.web.crud.annotation.Reactive;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.index.CandidateComponentsIndex;
import org.springframework.context.index.CandidateComponentsIndexLoader;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import javax.persistence.Table;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class EasyormRepositoryRegistrar implements ImportBeanDefinitionRegistrar {

    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private final MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();

    @SneakyThrows
    private Stream<Resource> doGetResources(String packageStr) {
        String path = ResourcePatternResolver
                .CLASSPATH_ALL_URL_PREFIX
                .concat(packageStr.replace(".", "/")).concat("/**/*.class");
        return Arrays.stream(resourcePatternResolver.getResources(path));
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("all")
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        Map<String, Object> attr = importingClassMetadata.getAnnotationAttributes(EnableEasyormRepository.class.getName());
        if (attr == null) {
            return;
        }
        boolean reactiveEnabled = Boolean.TRUE.equals(attr.get("reactive"));
        boolean nonReactiveEnabled = Boolean.TRUE.equals(attr.get("nonReactive"));

        String[] arr = (String[]) attr.get("value");
//        Set<Resource> resources = Arrays
//                .stream(arr)
//                .flatMap(this::doGetResources)
//                .collect(Collectors.toSet());

        Class<Annotation>[] anno = (Class[]) attr.get("annotation");

        Set<EntityInfo> entityInfos = new HashSet<>();
        CandidateComponentsIndex index = CandidateComponentsIndexLoader.loadIndex(org.springframework.util.ClassUtils.getDefaultClassLoader());
        Set<String> entities = Stream
                .of(arr)
                .flatMap(_package -> {
                    return index
                            .getCandidateTypes(_package, Table.class.getName())
                            .stream();
                })
                .collect(Collectors.toSet());
        for (String className : entities) {
//            MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
//            String className = reader.getClassMetadata().getClassName();
            Class<?> entityType = org.springframework.util.ClassUtils.forName(className, null);
            if (Arrays.stream(anno)
                      .noneMatch(ann -> AnnotationUtils.findAnnotation(entityType, ann) != null)) {
                continue;
            }

            ImplementFor implementFor = AnnotationUtils.findAnnotation(entityType, ImplementFor.class);
            Reactive reactive = AnnotationUtils.findAnnotation(entityType, Reactive.class);
            Class genericType = Optional
                    .ofNullable(implementFor)
                    .map(ImplementFor::value)
                    .orElseGet(() -> {
                        return Stream
                                .of(entityType.getInterfaces())
                                .filter(e -> GenericEntity.class.isAssignableFrom(e))
                                .findFirst()
                                .orElse(entityType);
                    });

            Class idType = null;
            if (implementFor == null || implementFor.idType() == Void.class) {
                try {
                    if (GenericEntity.class.isAssignableFrom(entityType)) {
                        idType = ClassUtils.getGenericType(entityType);
                    }
                    if (idType == null) {
                        Method getId = org.springframework.util.ClassUtils.getMethod(entityType, "getId");
                        idType = getId.getReturnType();
                    }
                } catch (Exception e) {
                    idType = String.class;
                }
            } else {
                idType = implementFor.idType();
            }

            EntityInfo entityInfo = new EntityInfo(genericType,
                                                   entityType,
                                                   idType,
                                                   reactiveEnabled,
                                                   nonReactiveEnabled);
            if (!entityInfos.contains(entityInfo) || implementFor != null) {
                entityInfos.add(entityInfo);
            }

        }
        for (EntityInfo entityInfo : entityInfos) {
            Class entityType = entityInfo.getEntityType();
            Class idType = entityInfo.getIdType();
            Class realType = entityInfo.getRealType();
            if (entityInfo.isReactive()) {
                log.trace("register ReactiveRepository<{},{}>", entityType.getName(), idType.getSimpleName());

                ResolvableType repositoryType = ResolvableType.forClassWithGenerics(DefaultReactiveRepository.class, entityType, idType);

                RootBeanDefinition definition = new RootBeanDefinition();
                definition.setTargetType(repositoryType);
                definition.setBeanClass(ReactiveRepositoryFactoryBean.class);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                definition.getPropertyValues().add("entityType", realType);
                registry.registerBeanDefinition(realType.getSimpleName().concat("ReactiveRepository"), definition);
            }
            if (entityInfo.isNonReactive()) {
                log.trace("register SyncRepository<{},{}>", entityType.getName(), idType.getSimpleName());
                ResolvableType repositoryType = ResolvableType.forClassWithGenerics(DefaultSyncRepository.class, entityType, idType);
                RootBeanDefinition definition = new RootBeanDefinition();
                definition.setTargetType(repositoryType);
                definition.setBeanClass(SyncRepositoryFactoryBean.class);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                definition.getPropertyValues().add("entityType", realType);
                registry.registerBeanDefinition(realType.getSimpleName().concat("SyncRepository"), definition);
            }

        }

        Map<Boolean, Set<EntityInfo>> group = entityInfos
                .stream()
                .collect(Collectors.groupingBy(EntityInfo::isReactive, Collectors.toSet()));

        for (Map.Entry<Boolean, Set<EntityInfo>> entry : group.entrySet()) {
            RootBeanDefinition definition = new RootBeanDefinition();
            definition.setTargetType(AutoDDLProcessor.class);
            definition.setBeanClass(AutoDDLProcessor.class);
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            definition.getPropertyValues().add("entities", entityInfos);
            definition.getPropertyValues().add("reactive", entry.getKey());
            definition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            definition.setSynthetic(true);
            registry.registerBeanDefinition(AutoDDLProcessor.class.getName() + "_" + count.incrementAndGet(), definition);
        }

//        try {
//            BeanDefinition definition = registry.getBeanDefinition(AutoDDLProcessor.class.getName());
//            Set<EntityInfo> infos = (Set) definition.getPropertyValues().get("entities");
//            infos.addAll(entityInfos);
//        } catch (NoSuchBeanDefinitionException e) {
//
//        }


    }

    static AtomicInteger count = new AtomicInteger();

}
