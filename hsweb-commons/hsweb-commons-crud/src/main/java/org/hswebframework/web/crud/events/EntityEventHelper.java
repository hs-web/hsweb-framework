package org.hswebframework.web.crud.events;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * 实体事件帮助器
 *
 * @author zhouhao
 * @since 4.0.12
 */
public class EntityEventHelper {

    private static final String doEventContextKey = EntityEventHelper.class.getName() + "_doEvent";

    /**
     * 判断当前是否设置了事件
     *
     * @param defaultIfEmpty 如果未设置时的默认值
     * @return 是否设置了事件
     */
    public static Mono<Boolean> isDoFireEvent(boolean defaultIfEmpty) {
        return Mono
                .subscriberContext()
                .flatMap(ctx -> Mono.justOrEmpty(ctx.<Boolean>getOrEmpty(doEventContextKey)))
                .defaultIfEmpty(defaultIfEmpty);
    }

    /**
     * 设置Mono不触发实体类事件
     *
     * <pre>
     *     save(...)
     *     .as(EntityEventHelper::setDoNotFireEvent)
     * </pre>
     *
     * @param stream 流
     * @param <T>    泛型
     * @return 流
     */
    public static <T> Mono<T> setDoNotFireEvent(Mono<T> stream) {
        return stream.subscriberContext(Context.of(doEventContextKey, false));
    }

    /**
     * 设置Flux不触发实体类事件
     * <pre>
     *     fetch()
     *     .as(EntityEventHelper::setDoNotFireEvent)
     * </pre>
     *
     * @param stream 流
     * @param <T>    泛型
     * @return 流
     */
    public static <T> Flux<T> setDoNotFireEvent(Flux<T> stream) {
        return stream.subscriberContext(Context.of(doEventContextKey, false));
    }
}
