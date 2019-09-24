package org.hswebframework.web.context;


import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @since 3.1.0
 */
public class ContextUtils {


    public static Mono<Context> currentContext() {
        return Mono.subscriberContext()
                .<Context>handle((context, sink) -> {
                    if (context.hasKey(Context.class)) {
                        sink.next(context.get(Context.class));
                    }
                })
                .subscriberContext(acceptContext(ctx->{

                }));
    }

    public static Function<reactor.util.context.Context, reactor.util.context.Context> acceptContext(Consumer<Context> contextConsumer) {
        return context -> {
            if (!context.hasKey(Context.class)) {
                context = context.put(Context.class, new MapContext());
            }
            contextConsumer.accept(context.get(Context.class));
            return context;
        };
    }


    public static void main(String[] args) {

        currentContext()
                .flatMap(ctx ->doRequest())
                .subscriberContext(acceptContext(ctx -> ctx.put(ContextKey.integer("test"), 100)))
                .subscribe(System.out::println);

    }

    public static Mono<Integer> doRequest() {
        return currentContext()
                .map(context -> context.get(ContextKey.integer("test")).orElse(0));
    }


}
