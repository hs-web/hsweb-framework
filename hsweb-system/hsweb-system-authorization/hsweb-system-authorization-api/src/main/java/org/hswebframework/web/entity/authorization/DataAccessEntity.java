package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.CloneableEntity;

/**
 * @author zhouhao
 */
public class DataAccessEntity implements CloneableEntity {
    private String action;

    private String describe;

    private String type;

    private String config;

    public String getAction() {
        return this.action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescribe() {
        return this.describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getConfig() {
        return this.config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public DataAccessEntity clone() {
        DataAccessEntity target = new DataAccessEntity();
        target.setDescribe(getDescribe());
        target.setAction(getAction());
        target.setConfig(getConfig());
        target.setType(getType());
        return target;
    }
}
