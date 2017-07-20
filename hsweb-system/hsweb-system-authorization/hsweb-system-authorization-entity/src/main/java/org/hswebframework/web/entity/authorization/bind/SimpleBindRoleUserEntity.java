package org.hswebframework.web.entity.authorization.bind;

import org.hswebframework.web.entity.authorization.SimpleUserEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleBindRoleUserEntity extends SimpleUserEntity implements BindRoleUserEntity {

    private List<String> roles;

    @Override
    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public SimpleBindRoleUserEntity clone() {
        SimpleBindRoleUserEntity target = ((SimpleBindRoleUserEntity) super.clone());
        if (roles != null)
            target.setRoles(new ArrayList<>(getRoles()));
        if (getProperties() != null)
            target.setProperties(new HashMap<>(getProperties()));
        return target;
    }
}
