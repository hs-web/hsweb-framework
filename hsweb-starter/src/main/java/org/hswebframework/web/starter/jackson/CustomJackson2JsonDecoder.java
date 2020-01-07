package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.reactivestreams.Publisher;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.DecodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.codec.HttpMessageDecoder;
import org.springframework.http.codec.json.Jackson2CodecSupport;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class CustomJackson2JsonDecoder extends Jackson2CodecSupport implements HttpMessageDecoder<Object> {

    /**
     * Constructor with a Jackson {@link ObjectMapper} to use.
     */
    public CustomJackson2JsonDecoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
    }


    @Override
    public boolean canDecode(ResolvableType elementType, @Nullable MimeType mimeType) {
        Type type = elementType.resolve() == null ? elementType.getType() : elementType.resolve();
        JavaType javaType = getObjectMapper().getTypeFactory().constructType(type);
        // Skip String: CharSequenceDecoder + "*/*" comes after
        return (!CharSequence.class.isAssignableFrom(elementType.toClass()) &&
                getObjectMapper().canDeserialize(javaType) && supportsMimeType(mimeType));
    }

    @Override
    public Flux<Object> decode(Publisher<DataBuffer> input, ResolvableType elementType,
                               @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

        ObjectMapper mapper = getObjectMapper();
        Flux<TokenBuffer> tokens = Jackson2Tokenizer.tokenize(
                Flux.from(input), mapper.getFactory(), mapper, true);

        ObjectReader reader = getObjectReader(elementType, hints);

        return tokens.handle((tokenBuffer, sink) -> {
            try {
                Object value = reader.readValue(tokenBuffer.asParser(getObjectMapper()));
                logValue(value, hints);
                if (value != null) {
                    sink.next(value);
                }
            } catch (IOException ex) {
                sink.error(processException(ex));
            }
        });
    }

    @Override
    public Mono<Object> decodeToMono(Publisher<DataBuffer> input, ResolvableType elementType,
                                     @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

        return DataBufferUtils.join(input)
                .map(dataBuffer -> decode(dataBuffer, elementType, mimeType, hints));
    }

    @Override
    public Object decode(DataBuffer dataBuffer, ResolvableType targetType,
                         @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) throws DecodingException {

        try {
            ObjectReader objectReader = getObjectReader(targetType, hints);
            Object value = objectReader.readValue(dataBuffer.asInputStream());
            logValue(value, hints);
            return value;
        } catch (IOException ex) {
            throw processException(ex);
        } finally {
            DataBufferUtils.release(dataBuffer);
        }
    }

    private ObjectReader getObjectReader(ResolvableType elementType, @Nullable Map<String, Object> hints) {
        Assert.notNull(elementType, "'elementType' must not be null");
        MethodParameter param = getParameter(elementType);
        Class<?> contextClass = (param != null ? param.getContainingClass() : null);
        Type type = elementType.resolve() == null ? elementType.getType() : elementType.resolve();

        JavaType javaType = getJavaType(type, contextClass);
        Class<?> jsonView = (hints != null ? (Class<?>) hints.get(Jackson2CodecSupport.JSON_VIEW_HINT) : null);
        return jsonView != null ?
                getObjectMapper().readerWithView(jsonView).forType(javaType) :
                getObjectMapper().readerFor(javaType);
    }

    private void logValue(@Nullable Object value, @Nullable Map<String, Object> hints) {
        if (!Hints.isLoggingSuppressed(hints)) {
            LogFormatUtils.traceDebug(logger, traceOn -> {
                String formatted = LogFormatUtils.formatValue(value, !traceOn);
                return Hints.getLogPrefix(hints) + "Decoded [" + formatted + "]";
            });
        }
    }

    private CodecException processException(IOException ex) {
        if (ex instanceof InvalidDefinitionException) {
            JavaType type = ((InvalidDefinitionException) ex).getType();
            return new CodecException("Type definition error: " + type, ex);
        }
        if (ex instanceof JsonProcessingException) {
            String originalMessage = ((JsonProcessingException) ex).getOriginalMessage();
            return new DecodingException("JSON decoding error: " + originalMessage, ex);
        }
        return new DecodingException("I/O error while parsing input stream", ex);
    }


    // HttpMessageDecoder...

    @Override
    public Map<String, Object> getDecodeHints(ResolvableType actualType, ResolvableType elementType,
                                              ServerHttpRequest request, ServerHttpResponse response) {

        return getHints(actualType);
    }

    @Override
    public List<MimeType> getDecodableMimeTypes() {
        return getMimeTypes();
    }

    // Jackson2CodecSupport ...

    @Override
    protected <A extends Annotation> A getAnnotation(MethodParameter parameter, Class<A> annotType) {
        return parameter.getParameterAnnotation(annotType);
    }

}
