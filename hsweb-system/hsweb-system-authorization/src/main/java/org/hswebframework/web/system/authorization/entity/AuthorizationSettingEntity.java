package org.hswebframework.web.system.authorization.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.ezorm.rdb.mapping.annotation.JsonCodec;
import org.hswebframework.web.crud.entity.Entity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.sql.JDBCType;
import java.util.List;
import java.util.Set;

@Table(name = "s_autz_setting_info", indexes = {
        @Index(name = "idx_sasi_dss", columnList = "dimension,setting_target,state desc")
})
@Getter
@Setter
public class AuthorizationSettingEntity implements Entity {
    @Id
    @Column(length = 32)
    private String id;

    @Column(length = 32, nullable = false, updatable = false)
    @Comment("权限ID")
    private String permission;

    @Column(length = 32, updatable = false)
    @Comment("维度")//如:user,role
    private String dimension;

    @Column(name = "dimension_name", length = 64)
    @Comment("维度名称")//如:用户,角色
    private String dimensionName;

    @Column(name = "setting_target", length = 32, updatable = false)
    @Comment("维度目标")//具体的某个维度实例ID
    private String settingTarget;

    @Column(name = "setting_target_name", length = 64, updatable = false)
    @Comment("维度目标名称")//维度实例名称.如: 用户名. 角色名
    private String settingTargetName;

    @Column(name = "state", nullable = false)
    @Comment("状态")
    private Byte state;

    @Column
    @ColumnType(jdbcType = JDBCType.CLOB)
    @JsonCodec
    @Comment("可操作权限")
    private Set<String> actions;

    @Column
    @ColumnType(jdbcType = JDBCType.CLOB)
    @JsonCodec
    @Comment("数据权限")
    private List<DataAccessEntity> dataAccesses;

}
