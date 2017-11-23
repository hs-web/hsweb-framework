package org.hswebframework.web.authorization.basic.web;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Predicate;

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
