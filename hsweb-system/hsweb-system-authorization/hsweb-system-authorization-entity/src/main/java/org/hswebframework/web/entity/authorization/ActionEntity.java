package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.CloneableEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ActionEntity implements CloneableEntity {

    private String action;

    private String describe;

    private boolean defaultCheck;

    public ActionEntity() {
    }

    public ActionEntity(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public boolean isDefaultCheck() {
        return defaultCheck;
    }

    public void setDefaultCheck(boolean defaultCheck) {
        this.defaultCheck = defaultCheck;
    }

    @Override
    public ActionEntity clone() {
        try {
            return (ActionEntity) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<ActionEntity> create(String... actions) {
        return Arrays.stream(actions).map(ActionEntity::new).collect(Collectors.toList());
    }

}