package org.hswebframework.web.dictionary.configuration;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.dict.EnumDict;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "hsweb.dict")
@Getter
@Setter
@Slf4j
public class DictionaryProperties {

    private Set<String> enumPackages = new HashSet<>();

    @SneakyThrows
    public List<Class> doScanEnum() {
        Set<String> packages = new HashSet<>(enumPackages);
        packages.add("org.hswebframework.web");
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        List<Class> classes = new ArrayList<>();
        for (String enumPackage : packages) {
            String path = "classpath*:" + ClassUtils.convertClassNameToResourcePath(enumPackage) + "/**/*.class";
            Resource[] resources = resourcePatternResolver.getResources(path);
            for (Resource resource : resources) {
                try {
                    MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
                    String name = reader.getClassMetadata().getClassName();
                    Class<?> clazz = ClassUtils.forName(name,null);
                    if (clazz.isEnum() && EnumDict.class.isAssignableFrom(clazz)) {
                        classes.add(clazz);
                    }
                } catch (Throwable e) {

                }
            }
        }
        metadataReaderFactory.clearCache();
        return classes;
    }
}
