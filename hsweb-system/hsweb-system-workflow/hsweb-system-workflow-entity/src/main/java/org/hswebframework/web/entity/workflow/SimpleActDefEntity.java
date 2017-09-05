package org.hswebframework.web.entity.workflow;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * @Author wangwei
 * @Date 2017/9/5.
 */
public class SimpleActDefEntity extends SimpleGenericEntity<String> implements ActDefEntity {

    private String actId;
    private String defId;

    @Override
    public String getActId() {
        return actId;
    }

    @Override
    public void setActId(String actId) {
        this.actId = actId;
    }

    @Override
    public String getDefId() {
        return defId;
    }

    @Override
    public void setDefId(String defId) {
        this.defId = defId;
    }
}
