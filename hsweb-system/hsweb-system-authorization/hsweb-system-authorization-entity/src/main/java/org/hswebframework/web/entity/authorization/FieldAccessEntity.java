package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.CloneableEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class FieldAccessEntity implements CloneableEntity {
    private String field;

    private String describe;

    private List<ActionEntity> actions;

    private boolean defaultCheck;

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
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

    public List<ActionEntity> getActions() {
        if (actions == null) actions = Collections.emptyList();
        return actions;
    }

    public void setActions(List<ActionEntity> actions) {
        this.actions = actions;
    }

    @Override
    public FieldAccessEntity clone() {
        FieldAccessEntity target = new FieldAccessEntity();
        target.setField(getField());
        target.setDescribe(getDescribe());
        target.setDefaultCheck(isDefaultCheck());
        if (actions != null) {
            target.setActions(actions.stream().map(ActionEntity::clone).collect(Collectors.toList()));
        }
        return target;
    }

}
