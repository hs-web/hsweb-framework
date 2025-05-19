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
import java.util.function.Supplier;
import java.util.stream.Stream;

@ConfigurationProperties(prefix = "hsweb.dict")
@Getter
@Setter
@Slf4j
public class DictionaryProperties {

    private Set<String> enumPackages = new HashSet<>();

    public DictionaryProperties() {
    }

    @SneakyThrows
    public Stream<Class<?>> doScanEnum() {
        Set<String> packages = new HashSet<>(enumPackages);
        packages.add("org.hswebframework.web");
        CachingMetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        // 获取主线程的类加载器
        Thread currentThread = Thread.currentThread();
        Supplier<ClassLoader> classLoaderSupplier = currentThread::getContextClassLoader;

        return packages
                .parallelStream()
                .flatMap(enumPackage -> {
                    // 在每个任务中设置一致的类加载器
                    ClassLoader prevClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(classLoaderSupplier.get());
                    String path = "classpath*:" + ClassUtils.convertClassNameToResourcePath(enumPackage) + "/**/*.class";
                    log.info("scan enum dict package:{}", path);
                    Resource[] resources;
                    try {
                        resources = resourcePatternResolver.getResources(path);
                    } catch (IOException e) {
                        log.warn("scan enum dict package:{} error:", path, e);
                        return Stream.empty();
                    }
                    Stream<? extends Class<?>> stream = Stream
                            .of(resources)
                            .map(resource -> {
                                try {
                                    // 在每个任务中设置一致的类加载器
                                    Thread.currentThread().setContextClassLoader(classLoaderSupplier.get());
                                    MetadataReader reader = metadataReaderFactory.getMetadataReader(resource);
                                    String name = reader.getClassMetadata().getClassName();
                                    Class<?> clazz = ClassUtils.forName(name, classLoaderSupplier.get());
                                    if (clazz.isEnum() && EnumDict.class.isAssignableFrom(clazz)) {
                                        return clazz;
                                    }
                                } catch (Throwable ignore) {

                                }
                                return null;
                            })
                            .filter(Objects::nonNull);
                    // 还原类加载器
                    Thread.currentThread().setContextClassLoader(prevClassLoader);
                    return stream;
                });

    }
}
