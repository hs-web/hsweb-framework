package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.CloneableEntity;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class OptionalField implements CloneableEntity {
    private String name;

    private String describe;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Override
    public OptionalField clone() {
        OptionalField optionalField = new OptionalField();
        optionalField.setName(name);
        optionalField.setDescribe(describe);
        return optionalField;
    }
}
