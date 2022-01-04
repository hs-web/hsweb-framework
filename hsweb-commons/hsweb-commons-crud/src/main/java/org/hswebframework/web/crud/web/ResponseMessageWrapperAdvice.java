package org.hswebframework.web.crud.web;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

@RestControllerAdvice
public class ResponseMessageWrapperAdvice implements ResponseBodyAdvice<Object> {
    @Setter
    @Getter
    private Set<String> excludes = new HashSet<>();

    @Override
    public boolean supports(@Nonnull MethodParameter methodParameter, @Nonnull Class<? extends HttpMessageConverter<?>> aClass) {

        if (methodParameter.getMethod() == null) {
            return true;
        }

        RequestMapping mapping = methodParameter.getMethodAnnotation(RequestMapping.class);
        if (mapping == null) {
            return false;
        }
        for (String produce : mapping.produces()) {
            MimeType mimeType = MimeType.valueOf(produce);
            if (MediaType.TEXT_EVENT_STREAM.includes(mimeType) ||
                    MediaType.APPLICATION_STREAM_JSON.includes(mimeType)) {
                return false;
            }
        }

        if (!CollectionUtils.isEmpty(excludes) && methodParameter.getMethod() != null) {

            String typeName = methodParameter.getMethod().getDeclaringClass().getName() + "." + methodParameter
                    .getMethod()
                    .getName();
            for (String exclude : excludes) {
                if (typeName.startsWith(exclude)) {
                    return false;
                }
            }
        }

        Class<?> returnType = methodParameter.getMethod().getReturnType();

        boolean isStream = Publisher.class.isAssignableFrom(returnType);
        if (isStream) {
            ResolvableType type = ResolvableType.forMethodParameter(methodParameter);
            returnType = type.resolveGeneric(0);
        }
        boolean isAlreadyResponse = returnType == ResponseMessage.class || returnType == ResponseEntity.class;

        return !isAlreadyResponse;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  @Nonnull MethodParameter returnType,
                                  @Nonnull MediaType selectedContentType,
                                  @Nonnull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @Nonnull ServerHttpRequest request,
                                  @Nonnull ServerHttpResponse response) {
        if (body instanceof Mono) {
            return ((Mono<?>) body)
                    .map(ResponseMessage::ok)
                    .switchIfEmpty(Mono.just(ResponseMessage.ok()));
        }
        if (body instanceof Flux) {
            return ((Flux<?>) body)
                    .collectList()
                    .map(ResponseMessage::ok)
                    .switchIfEmpty(Mono.just(ResponseMessage.ok()));
        }
        if (body instanceof String) {
            return JSON.toJSONString(ResponseMessage.ok(body));
        }
        return ResponseMessage.ok(body);
    }


}
