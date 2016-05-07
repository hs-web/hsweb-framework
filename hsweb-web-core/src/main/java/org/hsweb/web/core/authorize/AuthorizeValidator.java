package org.hsweb.web.core.authorize;

import org.hsweb.web.bean.po.user.User;

import java.util.Map;

/**
 * 权限验证器
 * Created by zhouhao on 16-4-28.
 */
public interface AuthorizeValidator {
    boolean validate(User user, Map<String, Object> param, AuthorizeValidatorConfig config);

    AuthorizeValidatorConfig createConfig();
}
