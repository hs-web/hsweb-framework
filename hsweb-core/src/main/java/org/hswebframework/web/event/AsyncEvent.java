package org.hswebframework.web.event;

import org.reactivestreams.Publisher;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

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
     * 推送事件到 ApplicationEventPublisher
     *
     * @param eventPublisher ApplicationEventPublisher
     * @return async void
     */
    Mono<Void> publish(ApplicationEventPublisher eventPublisher);
}
