package org.hswebframework.web.crud.web;

import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

public class ResponseMessageWrapper extends ResponseBodyResultHandler {
    public ResponseMessageWrapper(List<HttpMessageWriter<?>> writers, RequestedContentTypeResolver resolver, ReactiveAdapterRegistry registry) {
        super(writers, resolver, registry);
        setOrder(90);
    }

    private static MethodParameter param;

    static {
        try {
            param = new MethodParameter(ResponseMessageWrapper.class
                    .getDeclaredMethod("methodForParams"), -1);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    private static Mono<ResponseMessage<?>> methodForParams() {
        return Mono.empty();
    }

    @Override
    public boolean supports(HandlerResult result) {
        boolean isAlreadyResponse = result.getReturnType().resolveGeneric(0) == ResponseMessage.class;
        boolean isMono = result.getReturnType().resolve() == Mono.class;
        return isMono && super.supports(result) && !isAlreadyResponse;
    }

    @Override
    @SuppressWarnings("all")
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        Object body = result.getReturnValue();
        if (body instanceof Mono) {
            body = ((Mono) body)
                    .switchIfEmpty(Mono.just(ResponseMessage.ok()))
                    .map(ResponseMessage::ok);
        }
        if (body == null) {
            body = Mono.just(ResponseMessage.ok());
        }
        return writeBody(body, param, exchange);

    }
}
