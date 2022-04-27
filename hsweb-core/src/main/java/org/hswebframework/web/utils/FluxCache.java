package org.hswebframework.web.utils;

import org.reactivestreams.Publisher;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class FluxCache {


    public static <T> Flux<T> cache(Flux<T> source, Function<Flux<T>, Publisher<?>> handler) {
        Disposable[] ref = new Disposable[1];
        Flux<T> cache = source
                .doFinally((s) -> ref[0] = null)
                .replay()
                .autoConnect(1, dis -> ref[0] = dis);
        return Mono
                .from(handler.apply(cache))
                .thenMany(cache)
                .doFinally((s) -> {
                    if (ref[0] != null) {
                        ref[0].dispose();
                    }
                });

    }

}
