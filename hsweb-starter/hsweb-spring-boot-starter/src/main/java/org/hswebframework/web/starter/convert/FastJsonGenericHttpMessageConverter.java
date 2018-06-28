package org.hswebframework.web.starter.convert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.parser.deserializer.JavaBeanDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializeFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.convert.CustomMessageConverter;
import org.hswebframework.web.dict.DictSupportApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FastJsonGenericHttpMessageConverter extends AbstractGenericHttpMessageConverter<Object> implements Ordered {

    public final static Charset UTF8 = Charset.forName("UTF-8");

//    @Autowired(required = false)
//    private DictSupportApi dictSupportApi;

    private Charset charset = UTF8;

    private SerializerFeature[] features = new SerializerFeature[0];

    private List<CustomMessageConverter> converters;

    public FastJsonGenericHttpMessageConverter() {
        super(new MediaType("application", "json", UTF8),
                new MediaType("application", "*+json", UTF8));
    }

    public void setConverters(List<CustomMessageConverter> converters) {
        this.converters = converters;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected boolean supports(Class<?> clazz) {

        return true;
    }

    @Override
    public boolean canRead(Type type, Class<?> contextClass, MediaType mediaType) {
        return type instanceof ParameterizedType && super.canRead(type, contextClass, mediaType);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        return read(clazz, clazz, inputMessage);
    }

    public Charset getCharset() {
        return this.charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public SerializerFeature[] getFeatures() {
        return features;
    }

    public void setFeatures(SerializerFeature... features) {
        this.features = features;
    }

    public Object readByString(Type type, String jsonStr) {
        return readByBytes(type, jsonStr.getBytes());
    }


    public Object readByBytes(Type type, byte[] bytes) {
        if (type == String.class) {
            return new String(bytes, charset);
        }
        if (type instanceof Class) {
            Class clazz = ((Class) type);
            if (null != converters) {
                CustomMessageConverter converter = converters.stream()
                        .filter(cvt -> cvt.support(clazz))
                        .findFirst()
                        .orElse(null);
                if (converter != null) {
                    return converter.convert(clazz, bytes);
                }
            }
        }
        Object object = JSON.parseObject(bytes, 0, bytes.length, charset.newDecoder(), type);
//        if (dictSupportApi != null) {
//            object = dictSupportApi.unwrap(object);
//        }
        return object;
    }

    @Override
    public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream in = inputMessage.getBody();
        byte[] buf = new byte[1024];
        for (; ; ) {
            int len = in.read(buf);
            if (len == -1) {
                break;
            }
            if (len > 0) {
                baos.write(buf, 0, len);
            }
        }
        byte[] bytes = baos.toByteArray();
        return readByBytes(type, bytes);
    }

    public String converter(Object obj) {
        if (obj instanceof String) {
            return (String) obj;
        }

        String text;
        String callback = ThreadLocalUtils.getAndRemove("jsonp-callback");
        if (obj instanceof ResponseMessage) {
            ResponseMessage message = (ResponseMessage) obj;
//            if (dictSupportApi != null) {
//                message.setResult(dictSupportApi.wrap(message.getResult()));
//            }
            text = JSON.toJSONString(obj, FastJsonHttpMessageConverter.parseFilter(message), features);
        } else {
//            if (dictSupportApi != null) {
//                obj = dictSupportApi.wrap(obj);
//            }
            text = JSON.toJSONString(obj, features);
        }
        if (!StringUtils.isNullOrEmpty(callback)) {
            text = new StringBuilder()
                    .append(callback)
                    .append("(").append(text).append(")")
                    .toString();
        }
        return text;
    }

    @Override
    protected void writeInternal(Object obj, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        OutputStream out = outputMessage.getBody();
        byte[] bytes = converter(obj).getBytes(charset);
        out.write(bytes);
        out.flush();
    }


}
