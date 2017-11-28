package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.DataAccessConfig;

/**
 * @author zhouhao
 * @see DataAccessConfig
 * @since 3.0
 */
public abstract class AbstractDataAccessConfig implements DataAccessConfig {

    private static final long serialVersionUID = -9025349704771557106L;

    private String action;

    @Override
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
