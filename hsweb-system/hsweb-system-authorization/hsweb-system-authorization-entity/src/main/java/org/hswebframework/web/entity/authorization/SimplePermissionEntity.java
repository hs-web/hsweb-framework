package org.hswebframework.web.entity.authorization;

import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimplePermissionEntity extends SimpleGenericEntity<String> implements PermissionEntity {
    @NotBlank
    private String name;

    private String describe;

    private Byte status;

    //可选事件
    private List<ActionEntity> actions;

    private List<DataAccessEntity> dataAccess;

    private List<FieldAccessEntity> fieldAccess;

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

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    @Override
    public List<ActionEntity> getActions() {
        return actions;
    }

    @Override
    public void setActions(List<ActionEntity> actions) {
        this.actions = actions;
    }

    @Override
    public List<DataAccessEntity> getDataAccess() {
        return this.dataAccess;
    }

    @Override
    public List<FieldAccessEntity> getFieldAccess() {
        return this.fieldAccess;
    }

    @Override
    public void setDataAccess(List<DataAccessEntity> dataAccess) {
        this.dataAccess = dataAccess;
    }

    @Override
    public void setFieldAccess(List<FieldAccessEntity> fieldAccess) {
        this.fieldAccess = fieldAccess;
    }

    @Override
    public SimplePermissionEntity clone() {
        SimplePermissionEntity target = (SimplePermissionEntity) super.clone();
        if (actions != null)
            target.setActions(getActions().stream().map(ActionEntity::clone).collect(Collectors.toList()));
        return target;
    }

}
