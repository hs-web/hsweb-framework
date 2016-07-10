package org.hsweb.web.core.message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.PropertyPreFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import org.hsweb.web.core.utils.ThreadLocalUtils;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.hsweb.commons.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FastJsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    public final static Charset UTF8 = Charset.forName("UTF-8");

    private Charset charset = UTF8;

    private SerializerFeature[] features = new SerializerFeature[0];

    public FastJsonHttpMessageConverter() {
        super(new MediaType("application", "json", UTF8),
                new MediaType("application", "*+json", UTF8));
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
        if (clazz == String.class) return new String(bytes, charset);
        return JSON.parseObject(bytes, 0, bytes.length, charset.newDecoder(), clazz);
    }

    public String converter(Object obj) {
        if (obj instanceof String) return (String) obj;
        String text;
        String callback = ThreadLocalUtils.get("jsonp-callback");
        ThreadLocalUtils.remove("jsonp-callback");
        if (obj instanceof ResponseMessage) {
            ResponseMessage message = (ResponseMessage) obj;
            if (message.isSuccess() && message.isOnlyData())
                obj = message.getData();
            text = JSON.toJSONString(obj, parseFilter(message), features);
            if (callback == null) callback = message.getCallback();
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

    protected PropertyPreFilter[] parseFilter(ResponseMessage responseMessage) {
        List<PropertyPreFilter> filters = new ArrayList<>();
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
        return filters.toArray(new PropertyPreFilter[filters.size()]);
    }

}
