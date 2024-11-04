package org.hswebframework.web.crud.web;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.MethodParameter;
import org.springframework.core.ReactiveAdapterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.HandlerResult;
import org.springframework.web.reactive.accept.RequestedContentTypeResolver;
import org.springframework.web.reactive.result.method.annotation.ResponseBodyResultHandler;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResponseMessageWrapper extends ResponseBodyResultHandler {

    public ResponseMessageWrapper(List<HttpMessageWriter<?>> writers,
                                  RequestedContentTypeResolver resolver,
                                  ReactiveAdapterRegistry registry) {
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

    @Setter
    @Getter
    private Set<String> excludes = new HashSet<>();

    @Override
    public boolean supports(@NonNull HandlerResult result) {

        if (!CollectionUtils.isEmpty(excludes) && result.getHandler() instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) result.getHandler();

            String typeName = method.getMethod().getDeclaringClass().getName() + "." + method.getMethod().getName();
            for (String exclude : excludes) {
                if (typeName.startsWith(exclude)) {
                    return false;
                }
            }
        }
        Class<?> gen = result.getReturnType().resolveGeneric(0);

        boolean isAlreadyResponse = gen == ResponseMessage.class || gen == ResponseEntity.class;

        boolean isStream = result.getReturnType().resolve() == Mono.class
            || result.getReturnType().resolve() == Flux.class;

        RequestMapping mapping = result.getReturnTypeSource()
                                       .getMethodAnnotation(RequestMapping.class);
        if (mapping == null) {
            return false;
        }
        for (String produce : mapping.produces()) {
            MimeType mimeType = MimeType.valueOf(produce);
            if (MediaType.TEXT_EVENT_STREAM.includes(mimeType) ||
                MediaType.APPLICATION_NDJSON.includes(mimeType)) {
                return false;
            }
        }

        return isStream
            && super.supports(result)
            && !isAlreadyResponse;
    }

    @Override
    @SuppressWarnings("all")
    public Mono<Void> handleResult(ServerWebExchange exchange, HandlerResult result) {
        Object body = result.getReturnValue();

        List<MediaType> accept = exchange.getRequest().getHeaders().getAccept();

        if (accept.contains(MediaType.TEXT_EVENT_STREAM)||
            accept.contains(MediaType.APPLICATION_NDJSON)) {
            return writeBody(body, result.getReturnTypeSource(), exchange);
        }

        String ignoreWrapper = exchange
            .getRequest()
            .getHeaders()
            .getFirst("X-Response-Wrapper");
        if ("Ignore".equals(ignoreWrapper)) {
            return writeBody(body, result.getReturnTypeSource(), exchange);
        }

        if (body instanceof Mono) {
            body = ((Mono) body)
                .map(ResponseMessage::ok)
                .switchIfEmpty(Mono.just(ResponseMessage.ok()));
        }
        if (body instanceof Flux) {
            body = ((Flux) body)
                .collectList()
                .map(ResponseMessage::ok)
                .switchIfEmpty(Mono.just(ResponseMessage.ok()));

        }
        if (body == null) {
            body = Mono.just(ResponseMessage.ok());
        }
        return writeBody(body, param, exchange);

    }
}
