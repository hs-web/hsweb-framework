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
import org.springframework.util.ClassUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

@ConfigurationProperties(prefix = "hsweb.dict")
@Getter
@Setter
@Slf4j
public class DictionaryProperties {

    private Set<String> enumPackages = new HashSet<>();

    @SneakyThrows
    public Stream<Class<?>> doScanEnum() {
        Set<String> packages = new HashSet<>(enumPackages);
        packages.add("org.hswebframework.web");
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();

        return packages
            .parallelStream()
            .flatMap(enumPackage -> {
                String path = "classpath*:" + ClassUtils.convertClassNameToResourcePath(enumPackage) + "/**/*.class";
                Resource[] resources;
                try {
                    resources = resourcePatternResolver.getResources(path);
                } catch (IOException e) {
                    return Stream.empty();
                }
                return Stream
                    .of(resources)
                    .map(resource -> {
                        try {
                            MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
                            String name = reader.getClassMetadata().getClassName();
                            Class<?> clazz = ClassUtils.forName(name, null);
                            if (clazz.isEnum() && EnumDict.class.isAssignableFrom(clazz)) {
                                return clazz;
                            }
                        } catch (Throwable e) {
                            log.warn("load enum class error", e);
                        }
                        return null;
                    })
                    .filter(Objects::nonNull);
            });

    }
}
