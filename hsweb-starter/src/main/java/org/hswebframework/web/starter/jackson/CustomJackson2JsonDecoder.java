package org.hswebframework.web.starter.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import com.fasterxml.jackson.databind.util.TokenBuffer;
import org.hswebframework.web.api.crud.entity.EntityFactory;
import org.hswebframework.web.i18n.LocaleUtils;
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
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomJackson2JsonDecoder extends Jackson2CodecSupport implements HttpMessageDecoder<Object> {

    private final EntityFactory entityFactory;

    /**
     * Constructor with a Jackson {@link ObjectMapper} to use.
     */
    public CustomJackson2JsonDecoder(EntityFactory entityFactory, ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
        this.entityFactory = entityFactory;
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
    @NonNull
    public Flux<Object> decode(@NonNull Publisher<DataBuffer> input, @NonNull ResolvableType elementType,
                               @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

        ObjectMapper mapper = getObjectMapper();
        Flux<TokenBuffer> tokens = Jackson2Tokenizer.tokenize(
                Flux.from(input), mapper.getFactory(), mapper, true);

        ObjectReader reader = getObjectReader(elementType, hints);

        return LocaleUtils
                .currentReactive()
                .flatMapMany(locale -> tokens
                        .handle((tokenBuffer, sink) -> {
                            LocaleUtils.doWith(locale, l -> {
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

                        }));
    }

    @Override
    @NonNull
    public Mono<Object> decodeToMono(@NonNull Publisher<DataBuffer> input, @NonNull ResolvableType elementType,
                                     @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

        return LocaleUtils
                .currentReactive()
                .flatMap(locale -> DataBufferUtils
                        .join(input)
                        .map(dataBuffer -> LocaleUtils
                                .doWith(dataBuffer,
                                        locale,
                                        (buf, l) -> decode(buf, elementType, mimeType, hints)))
                );
    }

    @Override
    @NonNull
    public Object decode(@NonNull DataBuffer dataBuffer, @NonNull ResolvableType targetType,
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

    private Type getRelType(Type type) {
        if (type instanceof Class) {
            Class<?> realType = entityFactory.getInstanceType(((Class<?>) type), false);
            if (realType != null) {
                return realType;
            }
        }
        return type;
    }

    private ObjectReader getObjectReader(ResolvableType elementType, @Nullable Map<String, Object> hints) {
        Assert.notNull(elementType, "'elementType' must not be null");
        MethodParameter param = getParameter(elementType);
        Class<?> contextClass = (param != null ? param.getContainingClass() : null);
        Type type = elementType.resolve() == null ? elementType.getType() : elementType.toClass();

        if (Iterable.class.isAssignableFrom(elementType.toClass())) {
            ResolvableType genType = elementType.getGeneric(0);
            type = ResolvableType
                    .forClassWithGenerics(
                            elementType.toClass(),
                            ResolvableType.forType(getRelType(genType.getType())))
                    .getType();
        } else {
            type = getRelType(type);
        }

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
    @NonNull
    public Map<String, Object> getDecodeHints(@NonNull ResolvableType actualType, @NonNull ResolvableType elementType,
                                              @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {

        return getHints(actualType);
    }

    @Override
    @NonNull
    public List<MimeType> getDecodableMimeTypes() {
        return getMimeTypes();
    }

    // Jackson2CodecSupport ...

    @Override
    protected <A extends Annotation> A getAnnotation(MethodParameter parameter, @NonNull Class<A> annotType) {
        return parameter.getParameterAnnotation(annotType);
    }

}
