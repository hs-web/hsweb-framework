package org.hswebframework.web.oauth2.server.auth;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationSupplier;
import org.hswebframework.web.authorization.basic.web.ReactiveUserTokenParser;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.hswebframework.web.oauth2.server.AccessTokenManager;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class ReactiveOAuth2AccessTokenParser implements ReactiveUserTokenParser, ReactiveAuthenticationSupplier {

    private final AccessTokenManager accessTokenManager;

    @Override
    public Mono<ParsedToken> parseToken(ServerWebExchange exchange) {

        String token = exchange.getRequest().getQueryParams().getFirst("access_token");
        if (!StringUtils.hasText(token)) {
            token = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (StringUtils.hasText(token)) {
                String[] typeAndToken = token.split("[ ]");
                if (typeAndToken.length == 2 && typeAndToken[0].equalsIgnoreCase("bearer")) {
                    token = typeAndToken[1];
                }
            }
        }

        if (StringUtils.hasText(token)) {
            return Mono.just(ParsedToken.of("oauth2", token));
        }

        return Mono.empty();
    }

    @Override
    public Mono<Authentication> get(String userId) {
        return Mono.empty();
    }

    @Override
    public Mono<Authentication> get() {
        return Mono
                .deferContextual(context -> context
                        .<ParsedToken>getOrEmpty(ParsedToken.class)
                        .filter(token -> "oauth2".equals(token.getType()))
                        .map(t -> accessTokenManager.getAuthenticationByToken(t.getToken()))
                        .orElse(Mono.empty()));
    }
}
