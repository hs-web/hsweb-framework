package org.hswebframework.web.datasource.switcher;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * 响应式数据源或 Schema 切换接口。
 *
 * @deprecated 动态切换能力已弃用，仅为已有调用保留。
 */
@Deprecated
public interface ReactiveSwitcher {

    <P extends Publisher<?>> P useLast(P publisher);

    <P extends Publisher<?>> P use(P publisher, String id);

    <P extends Publisher<?>> P useDefault(P publisher);

    Mono<String> current();

    <P extends Publisher<?>> P reset(P publisher);

}
