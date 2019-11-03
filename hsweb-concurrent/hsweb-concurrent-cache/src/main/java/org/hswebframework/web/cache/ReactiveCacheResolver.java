package org.hswebframework.web.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheOperationInvocationContext;

import java.util.Collection;

public interface ReactiveCacheResolver {
    Collection<? extends ReactiveCache> resolveCaches(CacheOperationInvocationContext<?> context);

}
