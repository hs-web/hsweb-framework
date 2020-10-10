package org.hswebframework.web.authorization.token;

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

    static ParsedToken of(String type, String token) {
        return SimpleParsedToken.of(type, token);
    }
}
