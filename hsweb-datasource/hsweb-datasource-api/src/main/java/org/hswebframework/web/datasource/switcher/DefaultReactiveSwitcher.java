package org.hswebframework.web.datasource.switcher;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.context.ContextKey;
import org.hswebframework.web.context.ContextUtils;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class DefaultReactiveSwitcher implements ReactiveSwitcher {

    private String name;

    private String defaultId;

    private String type;

    public DefaultReactiveSwitcher(String name,String type) {
        this.name = "ReactiveSwitcher.".concat(name);
        this.defaultId = name.concat(".").concat("_default");
        this.type=type;
    }

    private <R> Mono<R> doInContext(Function<Deque<String>, Mono<R>> function) {
        return ContextUtils.reactiveContext()
                .map(ctx -> ctx.getOrDefault(ContextKey.<Deque<String>>of(this.name), LinkedList::new))
                .flatMap(function);
    }

    @SuppressWarnings("all")
    private <R extends Publisher<?>> R  doInContext(R publisher, Consumer<Deque<String>> consumer) {
        if (publisher instanceof Mono) {
            return (R)((Mono<?>) publisher)
                    .subscriberContext(ContextUtils.acceptContext(ctx -> {
                        consumer.accept(ctx.getOrDefault(ContextKey.<Deque<String>>of(this.name), LinkedList::new));
                    }));
        } else if (publisher instanceof Flux) {
            return (R)((Flux<?>) publisher)
                    .subscriberContext(ContextUtils.acceptContext(ctx -> {
                        consumer.accept(ctx.getOrDefault(ContextKey.<Deque<String>>of(this.name), LinkedList::new));
                    }));
        }
        return publisher;
    }

    @Override
    public <P extends Publisher<?>> P  useLast(P publisher) {
        return doInContext(publisher,queue -> {
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
        return doInContext(publisher,queue-> queue.addLast(id));
    }

    @Override
    public <P extends Publisher<?>> P  useDefault(P publisher) {
        return  use(publisher,defaultId);
    }

    @Override
    public <P extends Publisher<?>> P  reset(P publisher) {
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
