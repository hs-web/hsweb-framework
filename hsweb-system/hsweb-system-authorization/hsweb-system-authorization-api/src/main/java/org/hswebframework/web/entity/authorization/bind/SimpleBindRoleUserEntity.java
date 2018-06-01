package org.hswebframework.web.entity.authorization.bind;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.entity.authorization.SimpleUserEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
public class SimpleBindRoleUserEntity extends SimpleUserEntity implements BindRoleUserEntity {

    private List<String> roles;

    @Override
    public SimpleBindRoleUserEntity clone() {
        SimpleBindRoleUserEntity target = ((SimpleBindRoleUserEntity) super.clone());
        if (roles != null) {
            target.setRoles(new ArrayList<>(getRoles()));
        }
        return target;
    }
}
