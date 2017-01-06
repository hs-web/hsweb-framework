package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleRoleEntity extends SimpleGenericEntity<String> implements RoleEntity {
    private String name;

    private String describe;

    private boolean enabled;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public SimpleGenericEntity<String> clone() {
        SimpleRoleEntity target = new SimpleRoleEntity();
        target.setId(getId());
        target.setName(getName());
        target.setDescribe(getDescribe());
        target.setProperties(cloneProperties());
        target.setEnabled(isEnabled());
        return target;
    }
}
