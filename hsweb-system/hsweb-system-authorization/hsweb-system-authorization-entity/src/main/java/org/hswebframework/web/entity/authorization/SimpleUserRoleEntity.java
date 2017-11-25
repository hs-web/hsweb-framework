package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
public class SimpleUserRoleEntity implements UserRoleEntity {

    private static final long serialVersionUID = -8831232608833695774L;

    private String userId;

    private String roleId;

    @Override
    public SimpleUserRoleEntity clone() {
        SimpleUserRoleEntity target = new SimpleUserRoleEntity();
        target.setRoleId(getRoleId());
        target.setUserId(getUserId());
        return target;
    }
}
