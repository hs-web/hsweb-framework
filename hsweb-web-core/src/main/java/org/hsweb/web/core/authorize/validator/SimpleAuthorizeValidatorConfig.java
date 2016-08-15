package org.hsweb.web.core.authorize.validator;

import org.hsweb.web.core.authorize.AuthorizeValidatorConfig;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.AuthorizeException;
import org.hsweb.web.core.exception.AuthorizeForbiddenException;
import org.hsweb.commons.StringUtils;
import org.hsweb.expands.script.engine.DynamicScriptEngine;
import org.hsweb.expands.script.engine.DynamicScriptEngineFactory;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by zhouhao on 16-4-28.
 */
public class SimpleAuthorizeValidatorConfig implements AuthorizeValidatorConfig {
    protected Set<String> modules;
    protected Set<String> roles;
    protected Set<String> actions;
    protected Set<Expression> expressions = new LinkedHashSet<>();
    protected Authorize.MOD mod;
    protected boolean apiSupport;

    @Override
    public AuthorizeValidatorConfig setApiSupport(boolean apiSupport) {
        this.apiSupport = apiSupport;
        return this;
    }

    @Override
    public AuthorizeValidatorConfig setModules(Set<String> modules) {
        this.modules = modules;
        return this;
    }

    @Override
    public AuthorizeValidatorConfig setRoles(Set<String> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public AuthorizeValidatorConfig setActions(Set<String> actions) {
        this.actions = actions;
        return this;
    }

    @Override
    public AuthorizeValidatorConfig setMod(Authorize.MOD mod) {
        this.mod = mod;
        return this;
    }

    @Override
    public AuthorizeValidatorConfig addExpression(String expression, String language) {
        if (StringUtils.isNullOrEmpty(expression)) return this;
        DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(language);
        try {
            String id = "authorize.validator." + expression.hashCode();
            engine.compile(id, expression);
            expressions.add(new Expression(id, language));
        } catch (Exception e) {
            throw new AuthorizeForbiddenException("compile expression error", e, 403);
        }
        return this;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public Set<String> getModules() {
        if (modules == null) modules = new LinkedHashSet<>();
        return modules;
    }

    public Set<String> getRoles() {
        if (roles == null) roles = new LinkedHashSet<>();
        return roles;
    }

    public Set<String> getActions() {
        if (actions == null) actions = new LinkedHashSet<>();
        return actions;
    }

    public Set<Expression> getExpressions() {
        if (expressions == null) expressions = new LinkedHashSet<>();
        return expressions;
    }

    public boolean isApiSupport() {
        return apiSupport;
    }

    public void setExpressions(Set<Expression> expressions) {
        this.expressions = expressions;
    }

    public Authorize.MOD getMod() {
        return mod;
    }

    @Override
    public boolean isEmpty() {
        return getModules().isEmpty() && getRoles().isEmpty() && getExpressions().isEmpty();
    }

    public static class Expression {
        private String id;
        private String language;

        public Expression() {
        }

        public Expression(String id, String language) {
            this.id = id;
            this.language = language;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getLanguage() {
            return language;
        }

        public void setLanguage(String language) {
            this.language = language;
        }
    }

}
