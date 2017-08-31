package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.Authentication;

/**
 *
 * 用户令牌生产器，用于在用户进行授权后生成令牌
 * @author zhouhao
 *
 */
public interface UserTokenGenerator {
    String getSupportTokenType();

    GeneratedToken generate(Authentication authentication);
}
