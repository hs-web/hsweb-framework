package org.hswebframework.web.entity.workflow;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * @Author wangwei
 * @Date 2017/9/5.
 */
public class SimpleActDefEntity extends SimpleGenericEntity<String> implements ActDefEntity {

    private String actId;
    private String formId;
    private String defId;
    private String type;

    @Override
    public String getActId() {
        return actId;
    }

    @Override
    public void setActId(String actId) {
        this.actId = actId;
    }

    @Override
    public String getFormId() {
        return formId;
    }

    @Override
    public void setFormId(String formId) {
        this.formId = formId;
    }

    @Override
    public String getDefId() {
        return defId;
    }

    @Override
    public void setDefId(String defId) {
        this.defId = defId;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }
}
