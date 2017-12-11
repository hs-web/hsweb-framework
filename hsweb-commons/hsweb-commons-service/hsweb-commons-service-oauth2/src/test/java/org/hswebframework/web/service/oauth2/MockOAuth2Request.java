package org.hswebframework.web.service.oauth2;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Request;
import org.hswebframework.web.authorization.oauth2.client.request.TokenExpiredCallBack;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
public class MockOAuth2Request implements OAuth2Request {


    private Function<String, OAuth2Response> responseGetter;


    public MockOAuth2Request(Function<String, OAuth2Response> responseGetter) {
        this.responseGetter = responseGetter;
    }

    @Override
    public OAuth2Request onRefreshTokenExpired(TokenExpiredCallBack refreshTokenExpiredCallBack) {
        return this;
    }

    @Override
    public OAuth2Request onTokenExpired(TokenExpiredCallBack callback) {
        return this;
    }

    @Override
    public OAuth2Request param(String name, Object value) {
        log.info("set param :{}={}", name, value);
        return this;
    }

    @Override
    public OAuth2Request params(Map<String, String> params) {
        log.info("set params :{}", params);
        return this;
    }

    @Override
    public OAuth2Response upload(String name, InputStream inputStream) {
        return responseGetter.apply("post");
    }

    @Override
    public OAuth2Response upload(String name, InputStream inputStream, String fileName) {
        return responseGetter.apply("post");
    }

    @Override
    public OAuth2Request requestBody(String value) {
        log.info("set request body :{}", value);
        return this;
    }

    @Override
    public OAuth2Request header(String name, String value) {
        return this;
    }

    @Override
    public OAuth2Request cookie(String cookie) {
        return this;
    }

    @Override
    public OAuth2Request contentType(String contentType) {
        return this;
    }

    @Override
    public OAuth2Request accept(String accept) {
        return this;
    }

    @Override
    public OAuth2Request timeout(long millisecond, Consumer<OAuth2Request> timeoutCallBack) {
        return this;
    }

    @Override
    public OAuth2Response get() {
        return responseGetter.apply("get");
    }

    @Override
    public OAuth2Response put() {
        return responseGetter.apply("put");
    }

    @Override
    public OAuth2Response post() {
        return responseGetter.apply("post");
    }

    @Override
    public OAuth2Response delete() {
        return responseGetter.apply("delete");
    }

    @Override
    public OAuth2Response patch() {
        return responseGetter.apply("patch");
    }
}
