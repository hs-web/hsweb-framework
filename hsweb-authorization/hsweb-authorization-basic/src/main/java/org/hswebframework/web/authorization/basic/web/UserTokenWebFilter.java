package org.hswebframework.web.authorization.basic.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.events.AuthorizationSuccessEvent;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.logger.ReactiveLogger;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@AllArgsConstructor
@Order(1)
public class UserTokenWebFilter implements WebFilter {

    private final List<ReactiveUserTokenParser> parsers = new ArrayList<>();

    private final Map<String, ReactiveUserTokenGenerator> tokenGeneratorMap = new HashMap<>();

    private final UserTokenManager userTokenManager;

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, WebFilterChain chain) {

        return Flux
            .fromIterable(parsers)
            .flatMap(parser -> parser.parseToken(exchange))
            .next()
            .map(token -> chain
                .filter(exchange)
                .contextWrite(Context.of(ParsedToken.class, token)))
            .defaultIfEmpty(chain.filter(exchange))
            .flatMap(Function.identity())
            .contextWrite(ReactiveLogger.start("requestId", exchange.getRequest().getId()));

    }

    @EventListener
    public void handleUserSign(AuthorizationSuccessEvent event) {
        ReactiveUserTokenGenerator generator = event
            .<String>getParameter("tokenType")
            .map(tokenGeneratorMap::get)
            .orElseGet(() -> tokenGeneratorMap.get("default"));
        if (generator != null) {
            GeneratedToken token = generator.generate(event.getAuthentication());
            event.getResult().putAll(token.getResponse());
            if (StringUtils.hasText(token.getToken())) {
                event.getResult().put("token", token.getToken());
                long expires = event
                    .getParameter("expires")
                    .map(String::valueOf)
                    .map(Long::parseLong)
                    .orElse(token.getTimeout());

                event.async(
                    userTokenManager
                        .signIn(token.getToken(), token.getType(), event
                            .getAuthentication()
                            .getUser()
                            .getId(), expires)
                        .doOnNext(t -> {
                            event.getResult().put("expires", t.getMaxInactiveInterval());
                            log.debug("user [{}] sign in", t.getUserId());
                        })
                        .then());
            }
        }

    }

    public void register(ReactiveUserTokenGenerator generator) {
        tokenGeneratorMap.put(generator.getTokenType(), generator);
    }

    public void register(ReactiveUserTokenParser parser) {
        parsers.add(parser);
    }

}
