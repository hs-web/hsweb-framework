package org.hsweb.web.authorize.validator;

import org.hsweb.web.authorize.AuthorizeValidator;
import org.hsweb.web.authorize.AuthorizeValidatorConfig;
import org.hsweb.web.authorize.ExpressionScopeBean;
import org.hsweb.web.authorize.annotation.Authorize;
import org.hsweb.web.bean.po.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.webbuilder.utils.common.StringUtils;
import org.webbuilder.utils.script.engine.DynamicScriptEngine;
import org.webbuilder.utils.script.engine.DynamicScriptEngineFactory;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * 权限验证器
 * Created by zhouhao on 16-4-28.
 */
public class SimpleAuthorizeValidator implements AuthorizeValidator {

    @Autowired(required = false)
    private Map<String, ExpressionScopeBean> expressionScopeBeanMap;

    @Override
    public boolean validate(User user, Map<String, Object> param, AuthorizeValidatorConfig config) {
        SimpleAuthorizeValidatorConfig validatorConfig = ((SimpleAuthorizeValidatorConfig) config);
        Set<String> modules = validatorConfig.getModules();
        Set<String> roles = validatorConfig.getRoles();
        Set<String> actions = validatorConfig.getActions();
        Set<SimpleAuthorizeValidatorConfig.Expression> expressions = validatorConfig.getExpressions();
        Authorize.MOD mod = validatorConfig.getMod();
        boolean access = false;
        //验证模块
        if (!modules.isEmpty()) {
            if (mod == Authorize.MOD.AND)
                access = modules.stream().allMatch(module ->
                        user.hasAccessModuleAction(module, actions.toArray(new String[actions.size()])));
            else access = modules.stream().anyMatch(module ->
                    user.hasAccessModuleAction(module, actions.toArray(new String[actions.size()])));
        }
        //验证角色
        if (!roles.isEmpty()) {
            if (mod == Authorize.MOD.AND)
                access = roles.stream().allMatch(role -> user.hasAccessRole(role));
            else
                access = roles.stream().anyMatch(role -> user.hasAccessRole(role));
        }
        //验证表达式
        if (!expressions.isEmpty()) {
            if (mod == Authorize.MOD.AND)
                access = expressions.stream().allMatch(expression -> {
                    DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(expression.getLanguage());
                    Map<String, Object> var = getExpressionRoot(user);
                    var.putAll(param);
                    return StringUtils.isTrue(engine.execute(expression.getId(), var).getResult());
                });
            else
                access = expressions.stream().anyMatch(expression -> {
                    DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(expression.getLanguage());
                    Map<String, Object> var = getExpressionRoot(user);
                    var.putAll(param);
                    return StringUtils.isTrue(engine.execute(expression.getId(), var).getResult());
                });
        }
        return access;
    }


    public Map<String, Object> getExpressionRoot(User user) {
        Map<String, Object> root = new HashMap<>();
        if (expressionScopeBeanMap != null)
            root.putAll(expressionScopeBeanMap);
        root.put("user", user);
        return root;
    }

    @Override
    public AuthorizeValidatorConfig createConfig() {
        return new SimpleAuthorizeValidatorConfig();
    }

}
