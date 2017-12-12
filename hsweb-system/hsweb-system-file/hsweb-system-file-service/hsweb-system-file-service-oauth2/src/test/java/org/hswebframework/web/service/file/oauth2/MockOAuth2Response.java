package org.hswebframework.web.service.file.oauth2;

import org.hswebframework.web.authorization.oauth2.client.request.ResponseConvertHandler;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.authorization.oauth2.client.response.ResponseConvert;
import org.hswebframework.web.authorization.oauth2.client.simple.provider.HswebResponseConvertSupport;
import org.hswebframework.web.authorization.simple.builder.SimpleAuthenticationBuilderFactory;
import org.hswebframework.web.authorization.simple.builder.SimpleDataAccessConfigBuilderFactory;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.BiConsumer;

public class MockOAuth2Response implements OAuth2Response {

    private InputStream result;

    private ResponseConvertHandler handler = new HswebResponseConvertSupport(new SimpleAuthenticationBuilderFactory(new SimpleDataAccessConfigBuilderFactory()));

    @Override
    public InputStream asStream() {
        return result;
    }

    public MockOAuth2Response(InputStream result) {
        this.result = result;
    }

    @Override
    public String asString() {
        return new String(asBytes());
    }

    @Override
    public byte[] asBytes() {
        try {
            return StreamUtils.copyToByteArray(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T as(ResponseConvert<T> convert) {
        return convert.convert(this);
    }

    @Override
    public <T> T as(Class<T> type) {
        return handler.convert(this, type);
    }

    @Override
    public <T> List<T> asList(Class<T> type) {
        return handler.convertList(this, type);
    }

    @Override
    public int status() {
        return 200;
    }

    @Override
    public OAuth2Response onError(BiConsumer<OAuth2Response, ErrorType> onError) {
        return this;
    }
}
