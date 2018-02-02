package org.hswebframework.web.organizational.authorization.simple;

import org.hswebframework.web.authorization.simple.AbstractDataAccessConfig;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.hswebframework.web.authorization.access.ScopeDataAccessConfig;

import java.util.Set;

/**
 * @author zhouhao
 * @since 3.0
 */
public class SimpleScopeDataAccessConfig extends AbstractDataAccessConfig implements ScopeDataAccessConfig {
    private static final long serialVersionUID = 1_0;
    private String      scopeType;
    private Set<Object> scope;
    private String      type;

    public SimpleScopeDataAccessConfig() {
    }

    public SimpleScopeDataAccessConfig(String scopeType) {
        this.scopeType = scopeType;
    }

    public SimpleScopeDataAccessConfig(String scopeType, Set<Object> scope) {
        this.scopeType = scopeType;
        this.scope = scope;
    }

    public SimpleScopeDataAccessConfig(String type, String scopeType, String action, Set<Object> scope) {
        this.scopeType = scopeType;
        this.scope = scope;
        this.type = type;
        setAction(action);
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
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
