package org.hswebframework.web.authorization.simple;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.authorization.access.DataAccessType;
import org.hswebframework.web.authorization.access.DefaultDataAccessType;
import org.hswebframework.web.authorization.access.ScopeDataAccessConfig;
import org.hswebframework.web.authorization.simple.AbstractDataAccessConfig;

import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class DimensionDataAccessConfig extends AbstractDataAccessConfig implements ScopeDataAccessConfig {

    private Set<Object> scope;

    private boolean children;

    /**
     * @see DimensionType#getId()
     */
    private String scopeType;

    @Override
    public DefaultDataAccessType getType() {
        return DefaultDataAccessType.DIMENSION_SCOPE;
    }
}
