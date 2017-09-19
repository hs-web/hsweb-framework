package org.hswebframework.web.cache.spring.fix;

import org.springframework.cache.annotation.CacheAnnotationParser;
import org.springframework.cache.interceptor.CacheOperation;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface FixUseSupperClassCacheAnnotationParser extends CacheAnnotationParser {

    Collection<CacheOperation> parseCacheAnnotations(Class targetClass, Method method);
}
