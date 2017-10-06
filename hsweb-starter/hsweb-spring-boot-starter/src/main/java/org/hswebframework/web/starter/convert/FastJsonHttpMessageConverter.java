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
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.commons.model.Model;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.utils.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FastJsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> implements Ordered {

    public final static Charset UTF8 = Charset.forName("UTF-8");

    private Charset charset = UTF8;

    private SerializerFeature[] features = new SerializerFeature[0];

    private EntityFactory entityFactory;

    public FastJsonHttpMessageConverter() {
        super(new MediaType("application", "json", UTF8),
                new MediaType("application", "*+json", UTF8));
    }

    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    public EntityFactory getEntityFactory() {
        return entityFactory;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return true;
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

    public Object readByString(Class<?> clazz, String jsonStr) {
        return readByBytes(clazz, jsonStr.getBytes());
    }

    public Object readByBytes(Class<?> clazz, byte[] bytes) {
//        if (clazz == String.class) return new String(bytes, charset);
//        if (entityFactory != null && (Entity.class.isAssignableFrom(clazz) || Model.class.isAssignableFrom(clazz))) {
//            @SuppressWarnings("unchecked")
//            Class tmp = entityFactory.getInstanceType(clazz);
//            if (tmp != null) clazz = tmp;
//        }
        return JSON.parseObject(bytes, 0, bytes.length, charset.newDecoder(), clazz);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException,
            HttpMessageNotReadableException {
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
        return readByBytes(clazz, bytes);
    }

    public String converter(Object obj) {
        if (obj instanceof String) return (String) obj;
        String text;
        String callback = ThreadLocalUtils.getAndRemove("jsonp-callback");
        if (obj instanceof ResponseMessage) {
            ResponseMessage message = (ResponseMessage) obj;
            text = JSON.toJSONString(obj, parseFilter(message), features);
        } else {
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
    protected void writeInternal(Object obj, HttpOutputMessage outputMessage) throws IOException,
            HttpMessageNotWritableException {
        OutputStream out = outputMessage.getBody();
        byte[] bytes = converter(obj).getBytes(charset);
        out.write(bytes);
        out.flush();
    }

    protected static SerializeFilter[] parseFilter(ResponseMessage<?> responseMessage) {
        List<SerializeFilter> filters = new ArrayList<>();
        if (responseMessage.getIncludes() != null)
            for (Map.Entry<Class<?>, Set<String>> classSetEntry : responseMessage.getIncludes().entrySet()) {
                SimplePropertyPreFilter filter = new SimplePropertyPreFilter(classSetEntry.getKey());
                filter.getIncludes().addAll(classSetEntry.getValue());
                filters.add(filter);
            }
        if (responseMessage.getExcludes() != null)
            for (Map.Entry<Class<?>, Set<String>> classSetEntry : responseMessage.getExcludes().entrySet()) {
                SimplePropertyPreFilter filter = new SimplePropertyPreFilter(classSetEntry.getKey());
                filter.getExcludes().addAll(classSetEntry.getValue());
                filters.add(filter);
            }
        PropertyFilter responseMessageFilter = (object, name, value) ->
                !(object instanceof ResponseMessage) || value != null;
        filters.add(responseMessageFilter);

        return filters.toArray(new SerializeFilter[filters.size()]);
    }

}
