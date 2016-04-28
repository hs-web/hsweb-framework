package org.hsweb.web.authorize;

import org.hsweb.web.authorize.annotation.Authorize;

import java.util.*;

/**
 * Created by zhouhao on 16-4-28.
 */
public interface AuthorizeValidatorConfig {
    AuthorizeValidatorConfig setModules(Set<String> modules);

    AuthorizeValidatorConfig setRoles(Set<String> roles);

    AuthorizeValidatorConfig setActions(Set<String> actions);

    AuthorizeValidatorConfig setMod(Authorize.MOD mod);

    AuthorizeValidatorConfig addExpression(String expression, String language);

    default AuthorizeValidatorConfig addAnnotation(Set<Authorize> authorizes) {
        Set<String> modules = new LinkedHashSet<>();
        Set<String> roles = new LinkedHashSet<>();
        Set<String> actions = new LinkedHashSet<>();
        authorizes.forEach(tmp -> {
            modules.addAll(Arrays.asList(tmp.module()));
            roles.addAll(Arrays.asList(tmp.role()));
            actions.addAll(Arrays.asList(tmp.action()));
            setMod(tmp.mod());
            addExpression(tmp.expression(), tmp.expressionLanguage());
        });
        setActions(actions).setModules(modules).setRoles(roles);
        return this;
    }

}
