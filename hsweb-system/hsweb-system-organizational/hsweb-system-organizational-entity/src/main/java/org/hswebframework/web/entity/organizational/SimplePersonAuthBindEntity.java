package org.hswebframework.web.entity.organizational;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimplePersonAuthBindEntity extends SimplePersonEntity implements PersonAuthBindEntity {
    private PersonUserEntity personUser;
    //职务ID集合
    private Set<String>      positionIds;

    @Override
    public PersonUserEntity getPersonUser() {
        return personUser;
    }

    @Override
    public void setPersonUser(PersonUserEntity personUser) {
        this.personUser = personUser;
    }

    @Override
    public Set<String> getPositionIds() {
        return positionIds;
    }

    @Override
    public void setPositionIds(Set<String> positionIds) {
        this.positionIds = positionIds;
    }
}
