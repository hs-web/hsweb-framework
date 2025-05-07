package org.hswebframework.web.logging;

import reactor.core.publisher.Mono;

import java.util.Optional;

public class AccessLoggerHolder {
    static final ThreadLocal<AccessLoggerInfo> HOLDER = new ThreadLocal<>();


    public static Mono<AccessLoggerInfo> currentReactive() {
        return Mono
            .deferContextual(ctx -> Mono
                .justOrEmpty(ctx
                                 .<AccessLoggerInfo>getOrEmpty(AccessLoggerInfo.class)
                                 .or(AccessLoggerHolder::current)));
    }

    public static Optional<AccessLoggerInfo> current() {
        return Optional.ofNullable(HOLDER.get());
    }

    public static void set(AccessLoggerInfo info) {
        HOLDER.set(info);
    }

    public static void remove() {
        HOLDER.remove();
    }
}
