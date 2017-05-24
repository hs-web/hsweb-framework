package org.hswebframework.web.organizational.authorization.simple;

import org.hswebframework.web.authorization.simple.AbstractDataAccessConfig;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.hswebframework.web.organizational.authorization.access.ScopeDataAccessConfig;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleScopeDataAccessConfig extends AbstractDataAccessConfig implements ScopeDataAccessConfig {
    private DataAccessType.ScopeType scopeType;
    private Set<String>              scope;
    private String                   type;

    @Override
    public DataAccessType.ScopeType getScopeType() {
        return scopeType;
    }

    public void setScopeType(DataAccessType.ScopeType scopeType) {
        this.scopeType = scopeType;
    }

    @Override
    public Set<String> getScope() {
        return scope;
    }

    public void setScope(Set<String> scope) {
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
