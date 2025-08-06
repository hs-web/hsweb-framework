package org.hswebframework.web.datasource.switcher;

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class DefaultReactiveSwitcher implements ReactiveSwitcher {

    private final String name;

    private final String defaultId;

    private final String type;

    public DefaultReactiveSwitcher(String name, String type) {
        this.name = "ReactiveSwitcher.".concat(name);
        this.defaultId = name.concat(".").concat("_default");
        this.type = type;
    }

    @Deprecated
    private <R> Mono<R> doInContext(Function<Deque<String>, Mono<R>> function) {

        return Mono
            .deferContextual(ctx -> function.apply(ctx
                                                       .<Deque<String>>getOrEmpty(this.name)
                                                       .orElseGet(LinkedList::new)));
    }

    @SuppressWarnings("all")
    private <R extends Publisher<?>> R doInContext(R publisher, Consumer<Deque<String>> consumer) {
        if (publisher instanceof Mono) {
            return (R) Mono
                .deferContextual(ctx -> {
                    Deque<String> deque = ctx.<Deque<String>>getOrEmpty(this.name).orElseGet(LinkedList::new);
                    consumer.accept(deque);
                    return ((Mono<R>) publisher)
                        .contextWrite(Context.of(name, deque));
                });
        } else if (publisher instanceof Flux) {
            return (R) Flux
                .deferContextual(ctx -> {
                    Deque<String> deque = ctx.<Deque<String>>getOrEmpty(this.name).orElseGet(LinkedList::new);
                    consumer.accept(deque);
                    return ((Flux<R>) publisher)
                        .contextWrite(Context.of(name, deque));
                });
        }
        return publisher;
    }

    @Override
    public <P extends Publisher<?>> P useLast(P publisher) {
        return doInContext(publisher, queue -> {
            // 没有上一次了
            if (queue.isEmpty()) {
                return;
            }
            //移除队尾,则当前的队尾则为上一次使用的配置
            queue.removeLast();
        });
    }


    @Override
    public <P extends Publisher<?>> P use(P publisher, String id) {
        return doInContext(publisher, queue -> queue.addLast(id));
    }

    @Override
    public <P extends Publisher<?>> P useDefault(P publisher) {
        return use(publisher, defaultId);
    }

    @Override
    public <P extends Publisher<?>> P reset(P publisher) {
        return doInContext(publisher, Collection::clear);
    }

    @Override
    public Mono<String> current() {
        return doInContext(queue -> {
            if (queue.isEmpty()) {
                return Mono.empty();
            }

            String activeId = queue.getLast();
            if (defaultId.equals(activeId)) {
                return Mono.empty();
            }
            return Mono.just(activeId);
        });
    }


}
