package org.hswebframework.web.organizational.authorization.simple;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.web.authorization.simple.AbstractDataAccessConfig;

import java.util.Set;

/**
 * @author zhouhao
 * @since 3.0.5
 */
@Getter
@Setter
@ToString
public class ScopeByUserDataAccessConfig extends AbstractDataAccessConfig {

    private static final long serialVersionUID = 6678003761927318688L;

    private String scopeType;

    private String scopeTypeName;

    private Set<String> scope;

    private boolean children;

    @Override
    public String getType() {
        return "SCOPE_BY_USER";
    }

    @Override
    public int hashCode() {
        return (toString() + getAction()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ScopeByUserDataAccessConfig && obj.hashCode() == hashCode();
    }

}
