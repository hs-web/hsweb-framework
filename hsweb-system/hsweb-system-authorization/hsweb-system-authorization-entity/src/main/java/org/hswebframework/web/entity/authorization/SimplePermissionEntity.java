package org.hswebframework.web.entity.authorization;

import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimplePermissionEntity extends SimpleGenericEntity<String> implements PermissionEntity<SimpleActionEntity> {
    @NotBlank
    private String name;

    private String describe;

    private byte status = 1;

    //可选事件
    private List<SimpleActionEntity> actions;

    public String getName() {
        return this.name;
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

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    @Override
    public List<SimpleActionEntity> getActions() {
        return actions;
    }

    @Override
    public void setActions(List<SimpleActionEntity> actions) {
        this.actions = actions;
    }

    @Override
    public SimplePermissionEntity clone() {
        SimplePermissionEntity target = new SimplePermissionEntity();
        target.setId(getId());
        if (actions != null)
            target.setActions(new ArrayList<>(getActions()));
        target.setDescribe(getDescribe());
        target.setStatus(getStatus());
        return target;
    }

}
