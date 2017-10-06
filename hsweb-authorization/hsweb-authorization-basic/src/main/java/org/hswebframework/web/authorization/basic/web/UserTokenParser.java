package org.hswebframework.web.authorization.basic.web;

import javax.servlet.http.HttpServletRequest;
import java.util.function.Predicate;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface UserTokenParser {

    ParsedToken parseToken(HttpServletRequest request);
}
