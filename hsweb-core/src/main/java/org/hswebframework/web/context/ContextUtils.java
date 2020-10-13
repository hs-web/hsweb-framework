package org.hswebframework.web.context;


import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @since 4.0.0
 */
public class ContextUtils {

    private static final ThreadLocal<Context> contextThreadLocal = ThreadLocal.withInitial(MapContext::new);

    public static Context currentContext() {
        return contextThreadLocal.get();
    }

    public static Mono<Context> reactiveContext() {
        return Mono.subscriberContext()
                .<Context>handle((context, sink) -> {
                    if (context.hasKey(Context.class)) {
                        sink.next(context.get(Context.class));
                    }else {
                        sink.complete();
                    }
                })
                .subscriberContext(acceptContext(ctx -> {

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

}
