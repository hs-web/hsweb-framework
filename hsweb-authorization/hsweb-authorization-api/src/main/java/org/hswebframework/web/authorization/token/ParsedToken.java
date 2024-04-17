package org.hswebframework.web.authorization.token;

import org.springframework.http.HttpHeaders;

import java.util.function.BiConsumer;

/**
 * 令牌解析结果
 *
 * @author zhouhao
 */
public interface ParsedToken {
    /**
     * @return 令牌
     */
    String getToken();

    /**
     * @return 令牌类型
     */
    String getType();

    /**
     * 将token应用到Http Header
     *
     * @param headers headers
     * @since 4.0.17
     */
    default void apply(HttpHeaders headers) {
        throw new UnsupportedOperationException("unsupported apply "+getType()+" token to headers");
    }

    static ParsedToken ofBearer(String token) {
        return SimpleParsedToken.of("bearer", token, HttpHeaders::setBearerAuth);
    }

    static ParsedToken of(String type, String token) {
        return of(type, token, (_header, _token) -> _header.set(HttpHeaders.AUTHORIZATION, type + " " + _token));
    }

    static ParsedToken of(String type, String token, BiConsumer<HttpHeaders, String> headerSetter) {
        return SimpleParsedToken.of(type, token, headerSetter);
    }
}
