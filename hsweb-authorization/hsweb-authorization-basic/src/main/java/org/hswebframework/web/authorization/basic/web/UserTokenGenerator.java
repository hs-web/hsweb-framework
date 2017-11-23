package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.Authentication;

/**
 *
 * 用户令牌生产器，用于在用户进行授权后生成令牌
 * @author zhouhao
 *
 */
public interface UserTokenGenerator {
    String TOKEN_TYPE_SESSION_ID = "sessionId";

    String TOKEN_TYPE_SIMPLE = "simple-token";

    String getSupportTokenType();

    GeneratedToken generate(Authentication authentication);
}
