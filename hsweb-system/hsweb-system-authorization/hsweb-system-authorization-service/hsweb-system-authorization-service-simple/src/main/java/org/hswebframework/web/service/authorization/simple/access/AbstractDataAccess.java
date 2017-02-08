package org.hswebframework.web.service.authorization.simple.access;

import org.hswebframework.web.authorization.access.DataAccess;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public abstract class AbstractDataAccess implements DataAccess {

    private String action;

    @Override
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
