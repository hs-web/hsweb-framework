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

    private Byte status;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescribe() {
        return describe;
    }

    @Override
    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Override
    public Byte getStatus() {
        return status;
    }

    @Override
    public void setStatus(Byte status) {
        this.status = status;
    }

    @Override
    public SimpleRoleEntity clone() {
        SimpleRoleEntity target = ((SimpleRoleEntity) super.clone());
        return target;
    }
}
