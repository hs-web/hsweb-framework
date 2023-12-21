package org.hswebframework.web.cache.supports;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.cache.ReactiveCache;
import org.reactivestreams.Publisher;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@SuppressWarnings("all")
@Slf4j
public class RedisReactiveCache<E> extends AbstractReactiveCache<E> {

    private ReactiveRedisOperations<Object, Object> operations;

    private String redisKey;

    private ReactiveCache<E> localCache;

    private String topicName;

    public RedisReactiveCache(String redisKey, ReactiveRedisOperations<Object, Object> operations, ReactiveCache<E> localCache) {
        this.operations = operations;
        this.localCache = localCache;
        this.redisKey = redisKey;
        operations
                .listenToChannel(topicName = ("_cache_changed:" + redisKey))
                .map(ReactiveSubscription.Message::getMessage)
                .cast(String.class)
                .subscribe(s -> {
                    if (s.equals("___all")) {
                        localCache.clear().subscribe();
                        return;
                    }
                    //清空本地缓存
                    localCache.evict(s).subscribe();
                });
    }

    @Override
    protected Mono<Object> getNow(Object key) {
        return (Mono) localCache.getMono(key, () -> (Mono) operations.opsForHash().get(redisKey, key));
    }

    @Override
    public Mono<Void> putNow(Object key, Object value) {
        return operations
                .opsForHash()
                .put(redisKey, key, value)
                .then(localCache.evict(key))
                .then(operations.convertAndSend(topicName, key))
                .then();
    }


    protected <T> Mono<T> handleError(Throwable error) {
        log.error(error.getMessage(), error);
        return Mono.empty();
    }

    @Override
    public Mono<Void> evictAll(Iterable<?> key) {
        return operations
                .opsForHash()
                .remove(redisKey, StreamSupport.stream(key.spliterator(), false).toArray())
                .then(localCache.evictAll(key))
                .flatMap(nil -> Flux
                        .fromIterable(key)
                        .flatMap(k -> operations.convertAndSend(topicName, key))
                        .then())
                .onErrorResume(err -> this.handleError(err));
    }

    @Override
    public Flux<E> getAll(Object... keys) {
        if (keys == null || keys.length == 0) {
            return operations
                    .opsForHash()
                    .values(redisKey)
                    .map(r -> (E) r);
        }
        return operations
                .opsForHash()
                .multiGet(redisKey, Arrays.asList(keys))
                .flatMapIterable(Function.identity())
                .map(r -> (E) r)
                .onErrorResume(err -> this.handleError(err));
    }


    @Override
    public Mono<Void> evict(Object key) {
        return operations
                .opsForHash()
                .remove(redisKey, key)
                .then(localCache.evict(key))
                .then(operations.convertAndSend(topicName, key))
                .onErrorResume(err -> this.handleError(err))
                .then();
    }

    @Override
    public Mono<Void> clear() {
        return operations
                .opsForHash()
                .delete(redisKey)
                .then(localCache.clear())
                .then(operations.convertAndSend(topicName, "___all"))
                .onErrorResume(err -> this.handleError(err))
                .then();
    }
}
