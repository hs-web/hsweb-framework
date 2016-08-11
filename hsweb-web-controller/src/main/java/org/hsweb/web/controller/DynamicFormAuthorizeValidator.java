package org.hsweb.web.controller;

import org.hsweb.web.bean.po.user.User;

import java.util.Map;

/**
 * Created by zhouhao on 16-5-16.
 */
public interface DynamicFormAuthorizeValidator {
    boolean validate(String formName, User user,Map<String,Object> params, String... actions);
}
