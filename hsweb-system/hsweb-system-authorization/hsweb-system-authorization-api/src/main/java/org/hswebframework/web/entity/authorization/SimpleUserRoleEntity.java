package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
@Table(name = "s_user_role")
public class SimpleUserRoleEntity implements UserRoleEntity {

    private static final long serialVersionUID = -8831232608833695774L;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "role_id")
    private String roleId;

    @Override
    @SneakyThrows
    public SimpleUserRoleEntity clone() {
        return (SimpleUserRoleEntity)super.clone();
    }
}
