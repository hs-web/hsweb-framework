package org.hsweb.web.controller;

import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.ExpressionScopeBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by zhouhao on 16-5-16.
 */
@Component("dynamicFormAuthorizeValidator")
public class DynamicFormAuthorizeValidatorConfiguration implements ExpressionScopeBean {

    @Autowired(required = false)
    private List<DynamicFormAuthorizeValidator> dynamicFormAuthorizeValidators;

    public boolean validate(String formName, User user, String... actions) {
        if (dynamicFormAuthorizeValidators != null) {
            for (DynamicFormAuthorizeValidator validator : dynamicFormAuthorizeValidators) {
                if (validator.validate(formName, user, actions)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
