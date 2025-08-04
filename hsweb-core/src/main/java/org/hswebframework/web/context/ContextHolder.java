package org.hswebframework.web.context;

import lombok.SneakyThrows;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.io.Closeable;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContextHolder {

    private static final List<ContextHolderSupport> supports;

    static {
        supports = ServiceLoader
            .load(ContextHolderSupport.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toList());
        supports.add(new ThreadLocalContextHolderSupport());
        supports.sort(Comparator.comparingInt(ContextHolderSupport::order));
    }

    public static Closeable makeCurrent(ContextView context) {
        for (ContextHolderSupport support : supports) {
            if (support.isSupport()) {
                return support.makeCurrent(context);
            }
        }
        throw new UnsupportedOperationException();

    }

    @SneakyThrows
    public static <T> T doInContext(Context context, Callable<T> call) {
        try (Closeable ignore = makeCurrent(context)) {
            return call.call();
        } catch (UndeclaredThrowableException e) {
            throw e.getCause();
        }
    }

    public static <T> Mono<T> wrap(Function<ContextView, Mono<T>> handler) {
        return Mono.deferContextual(ctx -> {
            Context context = current().putAll(ctx);
            return handler.apply(context);
        });
    }

    public static Context current() {
        for (ContextHolderSupport support : supports) {
            if (support.isSupport()) {
                return support.current();
            }
        }
        throw new UnsupportedOperationException();
    }

    public interface ContextHolderSupport {
        boolean isSupport();

        Closeable makeCurrent(ContextView context);

        void clean();

        Context current();

        default int order() {
            return Integer.MIN_VALUE;
        }
    }

}
