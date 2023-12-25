package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.token.ParsedToken;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 令牌解析器，用于在接受到请求到时候，从请求中获取令牌
 * @author zhouhao
 * @see 3.0
 * @see ParsedToken
 * @see AuthorizedToken
 */
public interface UserTokenParser {
    ParsedToken parseToken(HttpServletRequest request);
}
