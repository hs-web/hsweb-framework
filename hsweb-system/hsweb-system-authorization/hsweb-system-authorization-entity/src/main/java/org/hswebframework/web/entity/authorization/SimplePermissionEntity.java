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

    private String type;

    //支持的数据权限控制类型
    private List<String> supportDataAccessTypes;

    //可选事件
    private List<ActionEntity> actions;

    private List<OptionalField> optionalFields;

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
    public void setOptionalFields(List<OptionalField> optionalFields) {
        this.optionalFields = optionalFields;
    }

    @Override
    public List<OptionalField> getOptionalFields() {
        return optionalFields;
    }

    @Override
    public SimplePermissionEntity clone() {
        SimplePermissionEntity target = (SimplePermissionEntity) super.clone();
        if (actions != null)
            target.setActions(getActions().stream().map(ActionEntity::clone).collect(Collectors.toList()));
        if (optionalFields != null) {
            target.setOptionalFields(getOptionalFields().stream().map(OptionalField::clone).collect(Collectors.toList()));
        }
        if (supportDataAccessTypes != null) {
            target.setSupportDataAccessTypes(new ArrayList<>(supportDataAccessTypes));
        }
        return target;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    public List<String> getSupportDataAccessTypes() {
        return supportDataAccessTypes;
    }

    public void setSupportDataAccessTypes(List<String> supportDataAccessTypes) {
        this.supportDataAccessTypes = supportDataAccessTypes;
    }
}
