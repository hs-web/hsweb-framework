package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.token.ParsedToken;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface ReactiveUserTokenParser {
    Mono<ParsedToken> parseToken(ServerWebExchange exchange);
}
