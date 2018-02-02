package org.hswebframework.web.organizational.authorization.simple;

import org.hswebframework.web.authorization.simple.AbstractDataAccessConfig;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;

import java.util.Set;

/**
 * 自定义范围配置
 *
 * @author zhouhao
 */
public class SimpleCustomScopeDataAccessConfig extends AbstractDataAccessConfig {
    private static final long serialVersionUID = 1_0;
    private Set<CustomScope> scope;

    @Override
    public String getType() {
        return DataAccessType.SCOPE_TYPE_CUSTOM;
    }

    public Set<CustomScope> getScope() {
        return scope;
    }

    public void setScope(Set<CustomScope> scope) {
        this.scope = scope;
    }
}
