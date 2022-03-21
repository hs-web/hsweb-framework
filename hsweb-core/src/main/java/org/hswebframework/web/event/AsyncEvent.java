package org.hswebframework.web.event;

import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.util.function.Function;

/**
 * 异步事件,使用响应式编程进行事件监听时,请使用此事件接口
 *
 * @author zhouhao
 * @since 4.0.5
 */
public interface AsyncEvent {

    Mono<Void> getAsync();

    /**
     * 注册一个异步任务
     *
     * @param publisher 异步任务
     */
    void async(Publisher<?> publisher);

    /**
     * 注册一个优先级高的任务
     * @param publisher 任务
     */
    void first(Publisher<?> publisher);

    void first(Function<Mono<?>,Publisher<?>> mapper);

    void async(Function<Mono<?>,Publisher<?>> mapper);

    /**
     * 推送事件到 ApplicationEventPublisher
     *
     * @param eventPublisher ApplicationEventPublisher
     * @return async void
     */
    Mono<Void> publish(ApplicationEventPublisher eventPublisher);
}
