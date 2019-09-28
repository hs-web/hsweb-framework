package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Table;
import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
@Table(name = "s_permission")
public class SimplePermissionEntity extends SimpleGenericEntity<String> implements PermissionEntity {
    private static final long serialVersionUID = -5505339187716888516L;
    @NotBlank
    @Column(length = 32)
    private String name;

    @Column
    private String describe;

    @Column
    private Byte status;

    @Column
    private String type;

    //支持的数据权限控制类型
    @Column(name = "spt_da_types")
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    private List<String> supportDataAccessTypes;

    //可选事件
    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    private List<ActionEntity> actions;

    @Column(name = "optional_fields")
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    private List<OptionalField> optionalFields;

    //直接关联其他权限
    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    private List<ParentPermission> parents;

    @Override
    public SimplePermissionEntity clone() {
        SimplePermissionEntity target = (SimplePermissionEntity) super.clone();
        if (actions != null) {
            target.setActions(getActions().stream().map(ActionEntity::clone).collect(Collectors.toList()));
        }
        if (parents != null) {
            target.setParents(new ArrayList<>(getParents()));
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
