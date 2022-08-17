package org.hswebframework.web.authorization.basic.web;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Getter
@Setter
public class DefaultUserTokenGenPar implements ReactiveUserTokenGenerator, ReactiveUserTokenParser {

    private long timeout = TimeUnit.MINUTES.toMillis(30);

    private String headerName = "X-Access-Token";

    @Override
    public String getTokenType() {
        return "default";
    }

    @Override
    public GeneratedToken generate(Authentication authentication) {
        String token = IDGenerator.MD5.generate();

        return new GeneratedToken() {
            @Override
            public Map<String, Object> getResponse() {
                return Collections.singletonMap("expires", timeout);
            }

            @Override
            public String getToken() {
                return token;
            }

            @Override
            public String getType() {
                return getTokenType();
            }

            @Override
            public long getTimeout() {
                return timeout;
            }
        };
    }

    @Override
    public Mono<ParsedToken> parseToken(ServerWebExchange exchange) {
        String token = Optional.ofNullable(exchange.getRequest()
                .getHeaders()
                .getFirst(headerName))
                .orElseGet(() -> exchange.getRequest().getQueryParams().getFirst(":X_Access_Token"));
        if (token == null) {
            return Mono.empty();
        }
        return Mono.just(ParsedToken.of(getTokenType(),token));
    }
}
