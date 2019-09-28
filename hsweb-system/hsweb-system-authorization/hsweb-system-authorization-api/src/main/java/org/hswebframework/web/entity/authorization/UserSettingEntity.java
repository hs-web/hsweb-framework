package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.ezorm.rdb.mapping.annotation.ColumnType;
import org.hswebframework.web.authorization.setting.UserSettingPermission;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

import javax.persistence.Column;
import javax.persistence.Table;
import java.sql.JDBCType;
import java.util.Date;

/**
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
@NoArgsConstructor
@Table(name = "s_user_setting")
public class UserSettingEntity extends SimpleGenericEntity<String> {
    @NotBlank(groups = CreateGroup.class)
    @Column(name = "user_id", length = 32)
    private String userId;

    @NotBlank(groups = CreateGroup.class)
    @Column
    private String key;

    @NotBlank(groups = CreateGroup.class)
    @Column(name = "setting_id", length = 32)
    private String settingId;

    @NotBlank(groups = CreateGroup.class)
    @Column(name = "setting")
    @ColumnType(jdbcType = JDBCType.LONGVARCHAR)
    private String setting;

    @Column
    private String describe;

    @Column
    private String name;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "permission")
    @ColumnType(jdbcType = JDBCType.VARCHAR, javaType = String.class)
    private UserSettingPermission permission;

    public boolean hasPermission(UserSettingPermission... permissions) {
        if (permission == null) {
            return true;
        }
        if (permission == UserSettingPermission.NONE) {
            return false;
        }

        return permission.in(permissions);

    }
}
