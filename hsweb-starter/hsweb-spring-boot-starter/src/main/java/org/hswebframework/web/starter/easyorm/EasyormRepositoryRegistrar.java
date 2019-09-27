package org.hswebframework.web.starter.easyorm;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.mapping.defaults.DefaultSyncRepository;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.classreading.SimpleMetadataReaderFactory;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class EasyormRepositoryRegistrar implements ImportBeanDefinitionRegistrar {

    private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

    private MetadataReaderFactory metadataReaderFactory = new SimpleMetadataReaderFactory();

    @Override
    @SneakyThrows
    @SuppressWarnings("all")
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {

        Map<String, Object> attr = importingClassMetadata.getAnnotationAttributes(EnableEasyormRepository.class.getName());
        if (attr == null) {
            return;
        }
        String[] arr = (String[]) attr.get("value");
        String path = Arrays.stream(arr)
                .map(str -> ResourcePatternResolver
                        .CLASSPATH_ALL_URL_PREFIX
                        .concat(str.replace(".", "/")).concat("/**/*.class"))
                .collect(Collectors.joining());

        Class<Annotation>[] anno = (Class[]) attr.get("annotation");
        boolean enableSync = Boolean.TRUE.equals(attr.get("enableSync"));

        List<Class> allEntities = new ArrayList<>();

        for (Resource resource : resourcePatternResolver.getResources(path)) {
            MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
            String className = reader.getClassMetadata().getClassName();
            Class entityType = Class.forName(className);
            if (Arrays.stream(anno)
                    .noneMatch(ann -> AnnotationUtils.findAnnotation(entityType, ann) != null)) {
                continue;
            }
            allEntities.add(entityType);
            ResolvableType repositoryType = ResolvableType.forClassWithGenerics(DefaultSyncRepository.class, entityType, String.class);

            log.debug("register easyorm synchronous repository for {}", entityType);

            RootBeanDefinition definition = new RootBeanDefinition();
            definition.setTargetType(repositoryType);
            definition.setBeanClass(SyncRepositoryFactoryBean.class);
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
            definition.getPropertyValues().add("entityType", entityType);
            registry.registerBeanDefinition(entityType.getSimpleName().concat("SyncRepository"), definition);


        }

        RootBeanDefinition definition = new RootBeanDefinition();
        definition.setTargetType(AutoDDLProcessor.class);
        definition.setBeanClass(AutoDDLProcessor.class);
        definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        definition.getPropertyValues().add("entities", allEntities);
        definition.setInitMethodName("init");
        registry.registerBeanDefinition(AutoDDLProcessor.class.getName(), definition);

    }


}
