package org.hswebframework.web.warn;

import lombok.AllArgsConstructor;
import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

@Getter
@AllArgsConstructor
public class Warning {

    private static final Object CONTEXT_KEY = Warning.class;

    private final String code;

    private final Object[] args;


    public static Context addWarnToContext(ContextView context, Supplier<Warning> warning) {
        Context ctx = createWarning(context);
        List<Warning> warnings = ctx.get(CONTEXT_KEY);
        warnings.add(warning.get());
        return ctx;
    }

    public static Context createWarning(ContextView context) {
        Context ctx = Context.of(context);
        if (!ctx.hasKey(CONTEXT_KEY)) {
            ctx = ctx.put(CONTEXT_KEY, new CopyOnWriteArrayList<>());
        }
        return ctx;
    }


    public static <T> Function<Throwable, Flux<T>> resumeFluxError(
        Throwable error,
        Function<Throwable, Warning> builder) {
        return err -> Flux.deferContextual(ctx -> {
            Warning warning = builder.apply(err);
            if (warning != null && ctx.hasKey(CONTEXT_KEY)) {
                ctx.<List<Warning>>get(CONTEXT_KEY).add(warning);
            }
            return Mono.empty();
        });
    }

    public static <T> Function<Throwable, Mono<T>> resumeMonoError(
        Throwable error,
        Function<Throwable, Warning> builder) {
        return err -> Mono.deferContextual(ctx -> {
            Warning warning = builder.apply(err);
            if (warning != null && ctx.hasKey(CONTEXT_KEY)) {
                ctx.<List<Warning>>get(CONTEXT_KEY).add(warning);
            }
            return Mono.empty();
        });
    }
}
