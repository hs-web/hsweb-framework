package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.token.DefaultUserTokenManager;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.hswebframework.web.logger.ReactiveLogger;
import org.junit.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class UserTokenWebFilterTest {

    @Test
    public void shouldUseFirstParsedTokenAndKeepLoggerContext() {
        UserTokenWebFilter filter = new UserTokenWebFilter(new DefaultUserTokenManager());
        ParsedToken firstToken = ParsedToken.of("api", "ak_test");
        AtomicInteger firstCalls = new AtomicInteger();
        AtomicInteger secondCalls = new AtomicInteger();
        AtomicReference<ParsedToken> actualToken = new AtomicReference<>();
        AtomicReference<String> requestId = new AtomicReference<>();
        MockServerWebExchange exchange = exchange();

        filter.register(current -> {
            firstCalls.incrementAndGet();
            return Mono.just(firstToken);
        });
        filter.register(current -> {
            secondCalls.incrementAndGet();
            return Mono.just(ParsedToken.of("bearer", "legacy"));
        });

        StepVerifier
            .create(filter.filter(exchange, captureContext(actualToken, requestId)))
            .verifyComplete();

        assertEquals(1, firstCalls.get());
        assertEquals(0, secondCalls.get());
        assertSame(firstToken, actualToken.get());
        assertEquals(exchange.getRequest().getId(), requestId.get());
    }

    @Test
    public void shouldTryNextParserWhenPreviousReturnsEmpty() {
        UserTokenWebFilter filter = new UserTokenWebFilter(new DefaultUserTokenManager());
        ParsedToken fallbackToken = ParsedToken.of("bearer", "legacy");
        AtomicInteger firstCalls = new AtomicInteger();
        AtomicInteger secondCalls = new AtomicInteger();
        AtomicReference<ParsedToken> actualToken = new AtomicReference<>();

        filter.register(current -> {
            firstCalls.incrementAndGet();
            return Mono.empty();
        });
        filter.register(current -> {
            secondCalls.incrementAndGet();
            return Mono.just(fallbackToken);
        });

        StepVerifier
            .create(filter.filter(exchange(), captureContext(actualToken, new AtomicReference<>())))
            .verifyComplete();

        assertEquals(1, firstCalls.get());
        assertEquals(1, secondCalls.get());
        assertSame(fallbackToken, actualToken.get());
    }

    @Test
    public void shouldStopParsingWhenParserFails() {
        UserTokenWebFilter filter = new UserTokenWebFilter(new DefaultUserTokenManager());
        IllegalStateException failure = new IllegalStateException("parse failed");
        AtomicInteger secondCalls = new AtomicInteger();
        AtomicInteger chainSubscriptions = new AtomicInteger();

        filter.register(current -> Mono.error(failure));
        filter.register(current -> {
            secondCalls.incrementAndGet();
            return Mono.just(ParsedToken.of("bearer", "legacy"));
        });

        StepVerifier
            .create(filter.filter(exchange(), current -> Mono.fromRunnable(chainSubscriptions::incrementAndGet)))
            .expectErrorMatches(error -> error == failure)
            .verify();

        assertEquals(0, secondCalls.get());
        assertEquals(0, chainSubscriptions.get());
    }

    private static MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/token-test").build());
    }

    private static WebFilterChain captureContext(AtomicReference<ParsedToken> token,
                                                 AtomicReference<String> requestId) {
        return exchange -> Mono.deferContextual(context -> {
            token.set(context.getOrDefault(ParsedToken.class, null));
            ReactiveLogger.log(context, values -> requestId.set(values.get("requestId")));
            return Mono.empty();
        });
    }
}
