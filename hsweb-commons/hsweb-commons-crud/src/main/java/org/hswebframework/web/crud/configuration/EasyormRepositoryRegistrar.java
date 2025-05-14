package org.hswebframework.web.crud.configuration;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.mapping.defaults.DefaultReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.DefaultSyncRepository;
import org.hswebframework.web.crud.annotation.EnableEasyormRepository;
import org.hswebframework.web.crud.annotation.Reactive;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ReflectionUtils;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class EasyormRepositoryRegistrar implements ImportBeanDefinitionRegistrar {

    private final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();

    static final boolean mvcEnabled;

    static {
        boolean mvcImported;
        try {
            Class.forName("org.springframework.web.servlet.DispatcherServlet");
            mvcImported = true;
        } catch (Throwable e) {
            mvcImported = false;
        }
        mvcEnabled = mvcImported;
    }

    private String getResourceClassName(Resource resource) {
        try {
            return metadataReaderFactory
                .getMetadataReader(resource)
                .getClassMetadata()
                .getClassName();
        } catch (IOException e) {
            return null;
        }
    }

    @SneakyThrows
    private Stream<Resource> doGetResources(String packageStr) {
        String path = ResourcePatternResolver
            .CLASSPATH_ALL_URL_PREFIX
            .concat(packageStr.replace(".", "/")).concat("/**/*.class");

        String clazz = ResourcePatternResolver
            .CLASSPATH_ALL_URL_PREFIX
            .concat(packageStr.replace(".", "/")).concat(".class");

        return Stream.concat(
            Arrays.stream(resourcePatternResolver.getResources(path)),
            Arrays.stream(resourcePatternResolver.getResources(clazz))
        );
    }

    protected Set<String> scanEntities(String[] packageStr) {
        return Stream
            .of(packageStr)
            .flatMap(this::doGetResources)
            .map(this::getResourceClassName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    private Class<?> findIdType(Class<?> entityType) {
        Class<?> idType;
        try {
            if (GenericEntity.class.isAssignableFrom(entityType)) {
                return GenericTypeResolver.resolveTypeArgument(entityType, GenericEntity.class);
            }

            Class<?>[] ref = new Class[1];
            ReflectionUtils.doWithFields(entityType, field -> {
                if (field.isAnnotationPresent(javax.persistence.Id.class)) {
                    ref[0] = field.getType();
                }
            });
            idType = ref[0];

            if (idType == null) {
                Method getId = org.springframework.util.ClassUtils.getMethod(entityType, "getId");
                idType = getId.getReturnType();
            }
        } catch (Throwable e) {
            log.warn("unknown id type of entity:{}", entityType);
            idType = String.class;
        }

        return idType;

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
        boolean nonReactiveEnabled = Boolean.TRUE.equals(attr.get("nonReactive")) || mvcEnabled;

        String[] arr = (String[]) attr.get("value");

        Class<Annotation>[] anno = (Class[]) attr.get("annotation");

        Set<EntityInfo> entityInfos = ConcurrentHashMap.newKeySet();
        for (String className : scanEntities(arr)) {
            Class<?> entityType = org.springframework.util.ClassUtils.forName(className, null);
            if (Arrays.stream(anno)
                      .noneMatch(ann -> AnnotationUtils.getAnnotation(entityType, ann) != null)) {
                continue;
            }

            Reactive reactive = AnnotationUtils.findAnnotation(entityType, Reactive.class);

            Class idType = findIdType(entityType);

            EntityInfo entityInfo = new EntityInfo(entityType,
                                                   entityType,
                                                   idType,
                                                   reactiveEnabled,
                                                   nonReactiveEnabled);
            if (!entityInfos.contains(entityInfo)) {
                entityInfos.add(entityInfo);
            }

        }
        for (EntityInfo entityInfo : entityInfos) {
            Class entityType = entityInfo.getEntityType();
            Class idType = entityInfo.getIdType();
            Class realType = entityInfo.getRealType();
            if (entityInfo.isReactive()) {
                String beanName = entityType.getSimpleName().concat("ReactiveRepository");
                log.trace("Register bean ReactiveRepository<{},{}> {}", entityType.getName(), idType.getSimpleName(), beanName);

                ResolvableType repositoryType = ResolvableType.forClassWithGenerics(DefaultReactiveRepository.class, entityType, idType);

                RootBeanDefinition definition = new RootBeanDefinition();
                definition.setTargetType(repositoryType);
                definition.setBeanClass(ReactiveRepositoryFactoryBean.class);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                definition.getPropertyValues().add("entityType", entityType);
                if (!registry.containsBeanDefinition(beanName)) {
                    registry.registerBeanDefinition(beanName, definition);
                } else {
                    entityInfos.remove(entityInfo);
                }
            }
            if (entityInfo.isNonReactive()) {
                String beanName = entityType.getSimpleName().concat("SyncRepository");
                log.trace("Register bean SyncRepository<{},{}> {}", entityType.getName(), idType.getSimpleName(), beanName);

                ResolvableType repositoryType = ResolvableType.forClassWithGenerics(DefaultSyncRepository.class, entityType, idType);
                RootBeanDefinition definition = new RootBeanDefinition();
                definition.setTargetType(repositoryType);
                definition.setBeanClass(SyncRepositoryFactoryBean.class);
                definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                definition.getPropertyValues().add("entityType", entityType);
                if (!registry.containsBeanDefinition(beanName)) {
                    registry.registerBeanDefinition(beanName, definition);
                } else {
                    entityInfos.remove(entityInfo);
                }
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

    }

    static AtomicInteger count = new AtomicInteger();

}
