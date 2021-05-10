package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.validator.CreateGroup;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.JDBCType;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Table(name = "s_autz_setting_info", indexes = {
        @Index(name = "idx_sasi_dss", columnList = "dimension_type,dimension_target,state desc"),
        @Index(name = "idx_sasi_pdd", columnList = "permission,dimension_type,dimension_target",unique = true)
})
@Getter
@Setter
public class AuthorizationSettingEntity implements Entity {
    @Id
    @Column(length = 32)
    @GeneratedValue(generator = "md5")
    private String id;

    @Column(length = 32, nullable = false, updatable = false)
    @Comment("权限ID")
    @NotBlank(message = "权限ID不能为空",groups = CreateGroup.class)
    @Schema(description = "权限ID")
    private String permission;

    @Column(name = "dimension_type",length = 32, nullable = false,updatable = false)
    @Comment("维度类型")//如:user,role
    @NotBlank(message = "维度不能为空",groups = CreateGroup.class)
    @Schema(description = "维度类型,如: user,role")
    private String dimensionType;

    @Column(name = "dimension_type_name", length = 64)
    @Comment("维度类型名称")//如:用户,角色
    @Schema(description = "维度类型名称,如: 用户,角色")
    private String dimensionTypeName;

    @Column(name = "dimension_target", length = 32, updatable = false)
    @Comment("维度目标")//具体的某个维度实例ID
    @NotBlank(message = "维度目标不能为空",groups = CreateGroup.class)
    @Schema(description = "维度目标,如: 用户的ID,角色的ID")
    private String dimensionTarget;

    @Column(name = "dimension_target_name", length = 64)
    @Comment("维度目标名称")//维度实例名称.如: 用户名. 角色名
    @Schema(description = "维度类型,如: 用户名,角色名")
    private String dimensionTargetName;

    @Column(name = "state", nullable = false)
    @Comment("状态")
    @NotNull(message = "状态不能为空",groups = CreateGroup.class)
    @Schema(description = "状态,0禁用,1启用")
    private Byte state;

    @Column
    @ColumnType(jdbcType = JDBCType.CLOB)
    @JsonCodec
    @Comment("可操作权限")
    @Schema(description = "授权可对此权限进行的操作")
    private Set<String> actions;

    @Column(name = "data_accesses")
    @ColumnType(jdbcType = JDBCType.CLOB)
    @JsonCodec
    @Comment("数据权限")
    @Schema(description = "数据权限配置")
    private List<DataAccessEntity> dataAccesses;

    @Column
    @Comment("优先级")
    @Schema(description = "冲突时,合并优先级")
    private Integer priority;

    @Column
    @Comment("是否合并")
    @Schema(description = "冲突时,是否合并")
    private Boolean merge;

    public AuthorizationSettingEntity copy(Predicate<String> actionFilter,
                                           Predicate<DataAccessEntity> dataAccessFilter){
        AuthorizationSettingEntity newSetting= FastBeanCopier.copy(this,new AuthorizationSettingEntity());
        if(!CollectionUtils.isEmpty(newSetting.getActions())){
            newSetting.setActions(newSetting.getActions().stream().filter(actionFilter).collect(Collectors.toSet()));
        }
        if(!CollectionUtils.isEmpty(newSetting.getDataAccesses())){
            newSetting.setDataAccesses(newSetting.getDataAccesses().stream().filter(dataAccessFilter).collect(Collectors.toList()));
        }
        return newSetting;
    }
}
