package org.hswebframework.web.crud.events;

import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.event.AsyncEvent;
import org.hswebframework.web.event.GenericsPayloadApplicationEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
                .deferContextual(ctx -> Mono.justOrEmpty(ctx.<Boolean>getOrEmpty(doEventContextKey)))
                .defaultIfEmpty(defaultIfEmpty);
    }

    public static Mono<Void> tryFireEvent(Supplier<Mono<Void>> task) {
        return Mono
                .deferContextual(ctx -> {
                    if (Boolean.TRUE.equals(ctx.getOrDefault(doEventContextKey, true))) {
                        return task.get();
                    }
                    return Mono.empty();
                });
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
        return stream.contextWrite(Context.of(doEventContextKey, false));
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
        return stream.contextWrite(Context.of(doEventContextKey, false));
    }

    public static <T> Mono<Void> publishSavedEvent(Object source,
                                                   Class<T> entityType,
                                                   List<T> entities,
                                                   Consumer<GenericsPayloadApplicationEvent<EntitySavedEvent<T>>> publisher) {
        return publishEvent(source, entityType, () -> new EntitySavedEvent<>(entities, entityType), publisher);
    }

    public static <T extends Entity> Mono<Void> publishModifyEvent(Object source,
                                                                   Class<T> entityType,
                                                                   List<T> before,
                                                                   Consumer<T> afterTransfer,
                                                                   Consumer<GenericsPayloadApplicationEvent<EntityModifyEvent<T>>> publisher) {
        return publishEvent(source,
                            entityType,
                            () -> new EntityModifyEvent<>(before,
                                                          before
                                                                  .stream()
                                                                  .map(t -> t.copyTo(entityType))
                                                                  .peek(afterTransfer)
                                                                  .collect(Collectors.toList()),
                                                          entityType),
                            publisher);
    }

    public static <T> Mono<Void> publishModifyEvent(Object source,
                                                    Class<T> entityType,
                                                    List<T> before,
                                                    List<T> after,
                                                    Consumer<GenericsPayloadApplicationEvent<EntityModifyEvent<T>>> publisher) {
        //没有数据被更新则不触发事件
        if (before.isEmpty()) {
            return Mono.empty();
        }
        return publishEvent(source, entityType, () -> new EntityModifyEvent<>(before, after, entityType), publisher);
    }

    public static <T> Mono<Void> publishDeletedEvent(Object source,
                                                     Class<T> entityType,
                                                     List<T> entities,
                                                     Consumer<GenericsPayloadApplicationEvent<EntityDeletedEvent<T>>> publisher) {
        return publishEvent(source, entityType, () -> new EntityDeletedEvent<>(entities, entityType), publisher);
    }

    public static <T> Mono<Void> publishCreatedEvent(Object source,
                                                     Class<T> entityType,
                                                     List<T> entities,
                                                     Consumer<GenericsPayloadApplicationEvent<EntityCreatedEvent<T>>> publisher) {
        return publishEvent(source, entityType, () -> new EntityCreatedEvent<>(entities, entityType), publisher);
    }

    public static <T, E extends AsyncEvent> Mono<Void> publishEvent(Object source,
                                                                    Class<T> entityType,
                                                                    Supplier<E> eventSupplier,
                                                                    Consumer<GenericsPayloadApplicationEvent<E>> publisher) {
        E event = eventSupplier.get();
        if (event == null) {
            return Mono.empty();
        }
        publisher.accept(new GenericsPayloadApplicationEvent<>(source, event, entityType));
        return event.getAsync();
    }
}
