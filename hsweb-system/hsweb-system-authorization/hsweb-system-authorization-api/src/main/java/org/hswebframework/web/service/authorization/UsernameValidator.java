package org.hswebframework.web.service.authorization;

import org.hswebframework.web.service.Validator;

/**
 * 用户名验证器,在保存用户信息的时候,用于验证用户名是否合法
 *
 * @author zhouhao
 * @since 3.0
 */
public interface UsernameValidator extends Validator<String> {
}
