package org.hswebframework.web.entity.workflow;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * @Author wangwei
 * @Date 2017/9/5.
 */
public class SimpleProcDefFormEntity extends SimpleGenericEntity<String> implements ProcDefFormEntity {

    String defKey;
    String formId;

    @Override
    public String getDefKey() {
        return defKey;
    }

    @Override
    public void setDefKey(String defKey) {
        this.defKey = defKey;
    }

    @Override
    public String getFormId() {
        return formId;
    }

    @Override
    public void setFormId(String formId) {
        this.formId = formId;
    }
}
