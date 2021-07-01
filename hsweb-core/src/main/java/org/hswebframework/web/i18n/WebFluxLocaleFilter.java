package org.hswebframework.web.i18n;

import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Locale;

public class WebFluxLocaleFilter implements WebFilter {
    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {
        return chain
                .filter(exchange)
                .subscriberContext(LocaleUtils.useLocale(getLocaleContext(exchange)));
    }

    public Locale getLocaleContext(ServerWebExchange exchange) {
        String lang = exchange.getRequest()
                              .getQueryParams()
                              .getFirst(":lang");
        if (StringUtils.hasText(lang)) {
            return Locale.forLanguageTag(lang);
        }
        Locale locale = exchange.getLocaleContext().getLocale();
        if (locale == null) {
            return Locale.getDefault();
        }
        return locale;
    }
}
