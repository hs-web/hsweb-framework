package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
public class SimplePermissionEntity extends SimpleGenericEntity<String> implements PermissionEntity {
    private static final long serialVersionUID = -5505339187716888516L;
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

    //直接关联其他权限
    private List<ParentPermission> parents;

    @Override
    public SimplePermissionEntity clone() {
        SimplePermissionEntity target = (SimplePermissionEntity) super.clone();
        if (actions != null) {
            target.setActions(getActions().stream().map(ActionEntity::clone).collect(Collectors.toList()));
        }
        if (optionalFields != null) {
            target.setOptionalFields(getOptionalFields().stream().map(OptionalField::clone).collect(Collectors.toList()));
        }
        if (supportDataAccessTypes != null) {
            target.setSupportDataAccessTypes(new ArrayList<>(supportDataAccessTypes));
        }
        return target;
    }

}
