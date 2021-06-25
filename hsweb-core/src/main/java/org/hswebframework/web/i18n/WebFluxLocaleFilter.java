package org.hswebframework.web.i18n;

import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleLocaleContext;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Optional;

public class WebFluxLocaleFilter implements WebFilter {
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        return chain
                .filter(exchange)
                .subscriberContext(ctx -> ctx.put(LocaleContext.class, getLocaleContext(exchange)));
    }

    public LocaleContext getLocaleContext(ServerWebExchange exchange) {
        String lang = exchange.getRequest()
                              .getQueryParams()
                              .getFirst(":lang");
        if (StringUtils.hasText(lang)) {
            return new SimpleLocaleContext(Locale.forLanguageTag(lang));
        }
        return exchange.getLocaleContext();
    }
}
