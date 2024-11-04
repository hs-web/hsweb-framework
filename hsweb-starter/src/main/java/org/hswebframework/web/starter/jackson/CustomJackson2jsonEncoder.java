package org.hswebframework.web.starter.jackson;

import org.hswebframework.web.i18n.LocaleUtils;
import org.springframework.http.codec.json.Jackson2CodecSupport;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.*;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CodecException;
import org.springframework.core.codec.EncodingException;
import org.springframework.core.codec.Hints;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeType;

/**
 * Base class providing support methods for Jackson 2.9 encoding. For non-streaming use
 * cases, {@link Flux} elements are collected into a {@link List} before serialization for
 * performance reason.
 *
 * @author Sebastien Deleuze
 * @author Arjen Poutsma
 * @since 5.0
 */
public class CustomJackson2jsonEncoder extends Jackson2CodecSupport implements HttpMessageEncoder<Object> {

    private static final byte[] NEWLINE_SEPARATOR = {'\n'};

    private static final Map<MediaType, byte[]> STREAM_SEPARATORS;

    private static final Map<String, JsonEncoding> ENCODINGS;

    static {
        STREAM_SEPARATORS = new HashMap<>(4);
        STREAM_SEPARATORS.put(MediaType.APPLICATION_NDJSON, NEWLINE_SEPARATOR);
        STREAM_SEPARATORS.put(MediaType.parseMediaType("application/stream+x-jackson-smile"), new byte[0]);

        ENCODINGS = new HashMap<>(JsonEncoding.values().length + 1);
        for (JsonEncoding encoding : JsonEncoding.values()) {
            ENCODINGS.put(encoding.getJavaName(), encoding);
        }
        ENCODINGS.put("US-ASCII", JsonEncoding.UTF8);
    }


    private final List<MediaType> streamingMediaTypes = new ArrayList<>(1);


    /**
     * Constructor with a Jackson {@link ObjectMapper} to use.
     */
    protected CustomJackson2jsonEncoder(ObjectMapper mapper, MimeType... mimeTypes) {
        super(mapper, mimeTypes);
        streamingMediaTypes.add(MediaType.APPLICATION_NDJSON);
    }


    /**
     * Configure "streaming" media types for which flushing should be performed
     * automatically vs at the end of the stream.
     * <p>By default this is set to {@link MediaType#APPLICATION_STREAM_JSON}.
     *
     * @param mediaTypes one or more media types to add to the list
     * @see HttpMessageEncoder#getStreamingMediaTypes()
     */
    public void setStreamingMediaTypes(List<MediaType> mediaTypes) {
        this.streamingMediaTypes.clear();
        this.streamingMediaTypes.addAll(mediaTypes);
    }


    @Override
    public boolean canEncode(ResolvableType elementType, @Nullable MimeType mimeType) {
        Class<?> clazz = elementType.toClass();
        if (!supportsMimeType(mimeType)) {
            return false;
        }
        if (mimeType != null && mimeType.getCharset() != null) {
            Charset charset = mimeType.getCharset();
            if (!ENCODINGS.containsKey(charset.name())) {
                return false;
            }
        }
        return (Object.class == clazz ||
                (!String.class.isAssignableFrom(elementType.resolve(clazz)) && getObjectMapper().canSerialize(clazz)));
    }

    @Override
    public Flux<DataBuffer> encode(Publisher<?> inputStream, DataBufferFactory bufferFactory,
                                   ResolvableType elementType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

        Assert.notNull(inputStream, "'inputStream' must not be null");
        Assert.notNull(bufferFactory, "'bufferFactory' must not be null");
        Assert.notNull(elementType, "'elementType' must not be null");

        if (inputStream instanceof Mono) {
            return Mono.from(inputStream)
                       .as(LocaleUtils::transform)
                       .map(value -> encodeValue(value, bufferFactory, elementType, mimeType, hints))
                       .flux();
        } else {
            byte[] separator = streamSeparator(mimeType);
            if (separator != null) { // streaming
                try {
                    ObjectWriter writer = createObjectWriter(elementType, mimeType, hints);
                    ByteArrayBuilder byteBuilder = new ByteArrayBuilder(writer
                                                                                .getFactory()
                                                                                ._getBufferRecycler());
                    JsonEncoding encoding = getJsonEncoding(mimeType);
                    JsonGenerator generator = getObjectMapper()
                            .getFactory()
                            .createGenerator(byteBuilder, encoding);
                    SequenceWriter sequenceWriter = writer.writeValues(generator);

                    return Flux
                            .from(inputStream)
                            .as(LocaleUtils::transform)
                            .map(value -> this.encodeStreamingValue(value,
                                                                    bufferFactory,
                                                                    hints,
                                                                    sequenceWriter,
                                                                    byteBuilder,
                                                                    separator))
                            .doAfterTerminate(() -> {
                                try {
                                    byteBuilder.release();
                                    generator.close();
                                } catch (IOException ex) {
                                    logger.error("Could not close Encoder resources", ex);
                                }
                            });
                } catch (IOException ex) {
                    return Flux.error(ex);
                }
            } else { // non-streaming
                ResolvableType listType = ResolvableType.forClassWithGenerics(List.class, elementType);
                return Flux.from(inputStream)
                           .collectList()
                           .as(LocaleUtils::transform)
                           .map(value -> encodeValue(value, bufferFactory, listType, mimeType, hints))
                           .flux();
            }

        }
    }

    @Override
    public DataBuffer encodeValue(Object value, DataBufferFactory bufferFactory,
                                  ResolvableType valueType, @Nullable MimeType mimeType, @Nullable Map<String, Object> hints) {

        ObjectWriter writer = createObjectWriter(valueType, mimeType, hints);
        ByteArrayBuilder byteBuilder = new ByteArrayBuilder(writer.getFactory()._getBufferRecycler());
        try {
            JsonEncoding encoding = getJsonEncoding(mimeType);

            logValue(hints, value);

            try (JsonGenerator generator = getObjectMapper().getFactory().createGenerator(byteBuilder, encoding)) {
                writer.writeValue(generator, value);
                generator.flush();
            } catch (InvalidDefinitionException ex) {
                throw new CodecException("Type definition error: " + ex.getType(), ex);
            } catch (JsonProcessingException ex) {
                throw new EncodingException("JSON encoding error: " + ex.getOriginalMessage(), ex);
            } catch (IOException ex) {
                throw new IllegalStateException("Unexpected I/O error while writing to byte array builder", ex);
            }

            byte[] bytes = byteBuilder.toByteArray();
            DataBuffer buffer = bufferFactory.allocateBuffer(bytes.length);
            buffer.write(bytes);

            return buffer;
        } finally {
            byteBuilder.release();
        }
    }

    private DataBuffer encodeStreamingValue(Object value, DataBufferFactory bufferFactory, @Nullable Map<String, Object> hints,
                                            SequenceWriter sequenceWriter, ByteArrayBuilder byteArrayBuilder, byte[] separator) {

        logValue(hints, value);

        try {
            sequenceWriter.write(value);
            sequenceWriter.flush();
        } catch (InvalidDefinitionException ex) {
            throw new CodecException("Type definition error: " + ex.getType(), ex);
        } catch (JsonProcessingException ex) {
            throw new EncodingException("JSON encoding error: " + ex.getOriginalMessage(), ex);
        } catch (IOException ex) {
            throw new IllegalStateException("Unexpected I/O error while writing to byte array builder", ex);
        }

        byte[] bytes = byteArrayBuilder.toByteArray();
        byteArrayBuilder.reset();

        int offset;
        int length;
        if (bytes.length > 0 && bytes[0] == ' ') {
            // SequenceWriter writes an unnecessary space in between values
            offset = 1;
            length = bytes.length - 1;
        } else {
            offset = 0;
            length = bytes.length;
        }
        DataBuffer buffer = bufferFactory.allocateBuffer(length + separator.length);
        buffer.write(bytes, offset, length);
        buffer.write(separator);

        return buffer;
    }

    private void logValue(@Nullable Map<String, Object> hints, Object value) {
        if (!Hints.isLoggingSuppressed(hints)) {
            LogFormatUtils.traceDebug(logger, traceOn -> {
                String formatted = LogFormatUtils.formatValue(value, !traceOn);
                return Hints.getLogPrefix(hints) + "Encoding [" + formatted + "]";
            });
        }
    }

    private ObjectWriter createObjectWriter(ResolvableType valueType, @Nullable MimeType mimeType,
                                            @Nullable Map<String, Object> hints) {

        JavaType javaType = getJavaType(valueType.getType(), null);
        Class<?> jsonView = (hints != null ? (Class<?>) hints.get(Jackson2CodecSupport.JSON_VIEW_HINT) : null);
        ObjectWriter writer = (jsonView != null ?
                getObjectMapper().writerWithView(jsonView) : getObjectMapper().writer());

        if (javaType.isContainerType()) {
            writer = writer.forType(javaType);
        }

        return customizeWriter(writer, mimeType, valueType, hints);
    }

    protected ObjectWriter customizeWriter(ObjectWriter writer, @Nullable MimeType mimeType,
                                           ResolvableType elementType, @Nullable Map<String, Object> hints) {

        return writer;
    }

    @Nullable
    private byte[] streamSeparator(@Nullable MimeType mimeType) {
        for (MediaType streamingMediaType : this.streamingMediaTypes) {
            if (streamingMediaType.isCompatibleWith(mimeType)) {
                return STREAM_SEPARATORS.getOrDefault(streamingMediaType, NEWLINE_SEPARATOR);
            }
        }
        return null;
    }

    /**
     * Determine the JSON encoding to use for the given mime type.
     *
     * @param mimeType the mime type as requested by the caller
     * @return the JSON encoding to use (never {@code null})
     * @since 5.0.5
     */
    protected JsonEncoding getJsonEncoding(@Nullable MimeType mimeType) {
        if (mimeType != null && mimeType.getCharset() != null) {
            Charset charset = mimeType.getCharset();
            JsonEncoding result = ENCODINGS.get(charset.name());
            if (result != null) {
                return result;
            }
        }
        return JsonEncoding.UTF8;
    }


    // HttpMessageEncoder

    @Override
    public List<MimeType> getEncodableMimeTypes() {
        return getMimeTypes();
    }

    @Override
    public List<MediaType> getStreamingMediaTypes() {
        return Collections.unmodifiableList(this.streamingMediaTypes);
    }

    @Override
    public Map<String, Object> getEncodeHints(@Nullable ResolvableType actualType, ResolvableType elementType,
                                              @Nullable MediaType mediaType, ServerHttpRequest request, ServerHttpResponse response) {

        return (actualType != null ? getHints(actualType) : Hints.none());
    }


    // Jackson2CodecSupport

    @Override
    protected <A extends Annotation> A getAnnotation(MethodParameter parameter, Class<A> annotType) {
        return parameter.getMethodAnnotation(annotType);
    }
}