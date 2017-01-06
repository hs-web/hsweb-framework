package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.CloneableEntity;

public interface ActionEntity extends CloneableEntity {
    String getAction();

    void setAction(String action);

    String getDescribe();

    void setDescribe(String describe);

    boolean isDefaultCheck();

    void setDefaultCheck(boolean defaultCheck);
}