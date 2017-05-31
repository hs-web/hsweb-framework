package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.FieldScopeDataAccessConfig;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleFiledScopeDataAccessConfig extends AbstractDataAccessConfig implements FieldScopeDataAccessConfig {

    private String scopeType;

    private Set<Object> scope;

    private String field;

    public SimpleFiledScopeDataAccessConfig() {
    }

    public SimpleFiledScopeDataAccessConfig(String field, Set<Object> scope) {
        this.scope = scope;
        this.field = field;
    }

    public SimpleFiledScopeDataAccessConfig(String field, String scopeType) {
        this.scopeType = scopeType;
        this.field = field;
    }

    @Override
    public String getScopeType() {
        return scopeType;
    }

    public void setScopeType(String scopeType) {
        this.scopeType = scopeType;
    }

    @Override
    public Set<Object> getScope() {
        return scope;
    }

    public void setScope(Set<Object> scope) {
        this.scope = scope;
    }

    @Override
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

}
