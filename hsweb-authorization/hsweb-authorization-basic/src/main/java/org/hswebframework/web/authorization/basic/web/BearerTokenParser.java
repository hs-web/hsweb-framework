package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.token.ParsedToken;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class BearerTokenParser implements ReactiveUserTokenParser {
    @Override
    public Mono<ParsedToken> parseToken(ServerWebExchange exchange) {

        String token = exchange
                .getRequest()
                .getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (token != null && token.startsWith("Bearer ")) {
            return Mono.just(ParsedToken.of("bearer", token.substring(7)));
        }
        return Mono.empty();
    }
}
