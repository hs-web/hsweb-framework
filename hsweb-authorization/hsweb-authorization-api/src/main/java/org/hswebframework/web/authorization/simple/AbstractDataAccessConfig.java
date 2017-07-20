package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.DataAccessConfig;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public abstract class AbstractDataAccessConfig implements DataAccessConfig {

    private String action;

    @Override
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
