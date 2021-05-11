package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.validator.CreateGroup;
import org.springframework.util.CollectionUtils;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.sql.JDBCType;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Table(name = "s_permission")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionEntity extends GenericEntity<String> {

    @Override
    @Pattern(regexp = "^[0-9a-zA-Z_\\-]+$", message = "ID只能由数字,字母,下划线和中划线组成", groups = CreateGroup.class)
    public String getId() {
        return super.getId();
    }

    @Column
    @Comment("权限名称")
    @Schema(description = "权限名称")
    @NotBlank(message = "权限名称不能为空", groups = CreateGroup.class)
    private String name;

    @Column
    @Comment("说明")
    @Schema(description = "说明")
    private String describe;

    @Column(nullable = false)
    @Comment("状态")
    @Schema(description = "状态")
    @DefaultValue("1")
    private Byte status;

    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    @Comment("可选操作")
    @Schema(description = "可选操作")
    private List<ActionEntity> actions;

    @Column(name = "optional_fields")
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    @Comment("可操作的字段")
    @Schema(description = "可操作字段")
    private List<OptionalField> optionalFields;

    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    @Comment("关联权限")
    @Schema(description = "关联权限")
    private List<ParentPermission> parents;

    @Column
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    @JsonCodec
    @Comment("其他配置")
    @Schema(description = "其他配置")
    private Map<String, Object> properties;

    public PermissionEntity copy(Predicate<ActionEntity> actionFilter,
                                 Predicate<OptionalField> fieldFilter) {
        PermissionEntity entity = FastBeanCopier.copy(this, new PermissionEntity());

        if (!CollectionUtils.isEmpty(entity.getActions())) {
            entity.setActions(entity.getActions().stream().filter(actionFilter).collect(Collectors.toList()));
        }
        if (!CollectionUtils.isEmpty(entity.getOptionalFields())) {
            entity.setOptionalFields(entity
                                             .getOptionalFields()
                                             .stream()
                                             .filter(fieldFilter)
                                             .collect(Collectors.toList()));
        }
        return entity;
    }
}
