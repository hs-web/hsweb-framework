package org.hswebframework.web.logger;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import reactor.core.publisher.*;
import reactor.util.context.Context;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class ReactiveLogger {

    private static String CONTEXT_KEY = ReactiveLogger.class.getName();

    public static Function<Context, Context> start(String key, String value) {
        return start(Collections.singletonMap(key, value));
    }

    public static Mono<Void> mdc(String key, String value) {
        return Mono.<Void>empty()
                .subscriberContext(start(key, value));
    }

    public static Function<Context, Context> start(Map<String, String> context) {
        return ctx -> {
            Optional<Map<String, String>> maybeContextMap = ctx.getOrEmpty(CONTEXT_KEY);
            if (maybeContextMap.isPresent()) {
                maybeContextMap.get().putAll(context);
                return ctx;
            } else {
                return ctx.put(CONTEXT_KEY, new LinkedHashMap<>(context));
            }
        };
    }


    public static <T> void log(Context context, Consumer<Map<String, String>> logger) {
        Optional<Map<String, String>> maybeContextMap = context.getOrEmpty(CONTEXT_KEY);
        if (!maybeContextMap.isPresent()) {
            logger.accept(new HashMap<>());
        } else {
            Map<String, String> ctx = maybeContextMap.get();
            MDC.setContextMap(ctx);
            try {
                logger.accept(ctx);
            } finally {
                MDC.clear();
            }
        }
    }

    public static <T> Consumer<Signal<T>> on(SignalType type, BiConsumer<Map<String, String>, Signal<T>> logger) {
        return signal -> {
            if (signal.getType() != type) {
                return;
            }
            Optional<Map<String, String>> maybeContextMap
                    = signal.getContext().getOrEmpty(CONTEXT_KEY);
            if (!maybeContextMap.isPresent()) {
                logger.accept(new HashMap<>(), signal);
            } else {
                Map<String, String> ctx = maybeContextMap.get();
                MDC.setContextMap(ctx);
                try {
                    logger.accept(ctx, signal);
                } finally {
                    MDC.clear();
                }
            }
        };
    }

    public static Mono<Void> mdc(Consumer<Map<String, String>> consumer) {
        return Mono.subscriberContext()
                .doOnNext(ctx -> {
                    Optional<Map<String, String>> maybeContextMap = ctx.getOrEmpty(CONTEXT_KEY);
                    if (maybeContextMap.isPresent()) {
                        consumer.accept(maybeContextMap.get());
                    } else {
                        consumer.accept(Collections.emptyMap());
                        log.warn("logger context is empty,please call publisher.subscriberContext(ReactiveLogger.mdc()) first!");
                    }
                })
                .then();
    }

    public static <T, R> BiConsumer<T, SynchronousSink<R>> handle(BiConsumer<T, SynchronousSink<R>> logger) {
        return (t, rFluxSink) -> {
            log(rFluxSink.currentContext(), context -> {
                logger.accept(t, rFluxSink);
            });
        };
    }

    public static <T> Consumer<Signal<T>> onNext(Consumer<T> logger) {
        return on(SignalType.ON_NEXT, (ctx, signal) -> {
            logger.accept(signal.get());
        });

    }

    public static <T> Consumer<Signal<T>> onComplete(Runnable logger) {
        return on(SignalType.ON_COMPLETE, (ctx, signal) -> {
            logger.run();
        });
    }

    public static <T> Consumer<Signal<T>> onError(Consumer<Throwable> logger) {
        return on(SignalType.ON_ERROR, (ctx, signal) -> {
            logger.accept(signal.getThrowable());
        });
    }


}
