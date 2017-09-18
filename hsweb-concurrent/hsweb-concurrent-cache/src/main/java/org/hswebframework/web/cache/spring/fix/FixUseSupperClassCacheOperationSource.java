/*
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hswebframework.web.cache.spring.fix;

import org.springframework.cache.annotation.CacheAnnotationParser;
import org.springframework.cache.interceptor.CacheOperation;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Implementation of the {@link org.springframework.cache.interceptor.CacheOperationSource
 * CacheOperationSource} interface for working with caching metadata in annotation format.
 * <p>
 * <p>This class reads Spring's {@link org.springframework.cache.annotation.Cacheable}, {@link org.springframework.cache.annotation.CachePut} and {@link org.springframework.cache.annotation.CacheEvict}
 * annotations and exposes corresponding caching operation definition to Spring's cache
 * infrastructure. This class may also serve as base class for a custom
 * {@code CacheOperationSource}.
 *
 * @author Costin Leau
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 3.1
 */
@SuppressWarnings("serial")
public class FixUseSupperClassCacheOperationSource extends FixUseSupperClassFallbackCacheOperationSource implements Serializable {

    private boolean publicMethodsOnly;

    private final Set<FixUseSupperClassCacheAnnotationParser> annotationParsers;


    /**
     * Create a default AnnotationCacheOperationSource, supporting public methods
     * that carry the {@code Cacheable} and {@code CacheEvict} annotations.
     */
    public FixUseSupperClassCacheOperationSource() {
        this(true);
    }

    /**
     * Create a default {@code AnnotationCacheOperationSource}, supporting public methods
     * that carry the {@code Cacheable} and {@code CacheEvict} annotations.
     *
     * @param publicMethodsOnly whether to support only annotated public methods
     *                          typically for use with proxy-based AOP), or protected/private methods as well
     *                          (typically used with AspectJ class weaving)
     */
    public FixUseSupperClassCacheOperationSource(boolean publicMethodsOnly) {
        this.publicMethodsOnly = publicMethodsOnly;
        this.annotationParsers = new LinkedHashSet<>(1);
        this.annotationParsers.add(new FixUseSupperClassAnnotationParser());
    }

    /**
     * Create a custom AnnotationCacheOperationSource.
     *
     * @param annotationParser the CacheAnnotationParser to use
     */
    public FixUseSupperClassCacheOperationSource(FixUseSupperClassCacheAnnotationParser annotationParser) {
        this.publicMethodsOnly = true;
        Assert.notNull(annotationParser, "CacheAnnotationParser must not be null");
        this.annotationParsers = Collections.singleton(annotationParser);
    }

    /**
     * Create a custom AnnotationCacheOperationSource.
     *
     * @param annotationParsers the CacheAnnotationParser to use
     */
    public FixUseSupperClassCacheOperationSource(FixUseSupperClassCacheAnnotationParser... annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
        Set<FixUseSupperClassCacheAnnotationParser> parsers = new LinkedHashSet<>(annotationParsers.length);
        Collections.addAll(parsers, annotationParsers);
        this.annotationParsers = parsers;
    }

    /**
     * Create a custom AnnotationCacheOperationSource.
     *
     * @param annotationParsers the CacheAnnotationParser to use
     */
    public FixUseSupperClassCacheOperationSource(Set<FixUseSupperClassCacheAnnotationParser> annotationParsers) {
        this.publicMethodsOnly = true;
        Assert.notEmpty(annotationParsers, "At least one CacheAnnotationParser needs to be specified");
        this.annotationParsers = annotationParsers;
    }


    @Override
    protected Collection<CacheOperation> findCacheOperations(Class<?> targetClass, Method method) {
        return determineCacheOperations(parser -> parser.parseCacheAnnotations(targetClass, method));
    }

    @Override
    protected Collection<CacheOperation> findCacheOperations(final Class<?> clazz) {
        return determineCacheOperations(parser -> parser.parseCacheAnnotations(clazz));
    }

//	@Override
//	protected Collection<CacheOperation> findCacheOperations(final Method method) {
//		return determineCacheOperations(parser -> parser.parseCacheAnnotations(method));
//	}

    /**
     * Determine the cache operation(s) for the given {@link CacheOperationProvider}.
     * <p>This implementation delegates to configured
     * {@link CacheAnnotationParser}s for parsing known annotations into
     * Spring's metadata attribute class.
     * <p>Can be overridden to support custom annotations that carry
     * caching metadata.
     *
     * @param provider the cache operation provider to use
     * @return the configured caching operations, or {@code null} if none found
     */
    protected Collection<CacheOperation> determineCacheOperations(CacheOperationProvider provider) {
        Collection<CacheOperation> ops = null;
        for (FixUseSupperClassCacheAnnotationParser annotationParser : this.annotationParsers) {
            Collection<CacheOperation> annOps = provider.getCacheOperations(annotationParser);
            if (annOps != null) {
                if (ops == null) {
                    ops = new ArrayList<>();
                }
                ops.addAll(annOps);
            }
        }
        return ops;
    }

    /**
     * By default, only public methods can be made cacheable.
     */
    @Override
    protected boolean allowPublicMethodsOnly() {
        return this.publicMethodsOnly;
    }


    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof FixUseSupperClassCacheOperationSource)) {
            return false;
        }
        FixUseSupperClassCacheOperationSource otherCos = (FixUseSupperClassCacheOperationSource) other;
        return (this.annotationParsers.equals(otherCos.annotationParsers) &&
                this.publicMethodsOnly == otherCos.publicMethodsOnly);
    }

    @Override
    public int hashCode() {
        return this.annotationParsers.hashCode();
    }

    public void setPublicMethodsOnly(boolean publicMethodsOnly) {
        this.publicMethodsOnly = publicMethodsOnly;
    }

    /**
     * Callback interface providing {@link CacheOperation} instance(s) based on
     * a given {@link CacheAnnotationParser}.
     */
    protected interface CacheOperationProvider {

        /**
         * Return the {@link CacheOperation} instance(s) provided by the specified parser.
         *
         * @param parser the parser to use
         * @return the cache operations, or {@code null} if none found
         */
        Collection<CacheOperation> getCacheOperations(FixUseSupperClassCacheAnnotationParser parser);
    }

}
