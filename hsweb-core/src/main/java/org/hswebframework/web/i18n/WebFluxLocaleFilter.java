package org.hswebframework.web.i18n;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.lang.NonNull;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class WebFluxLocaleFilter implements WebFilter {
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        return chain
                .filter(exchange)
                .subscriberContext(ctx -> ctx.put(LocaleContext.class, exchange.getLocaleContext()));
    }
}
