package org.hswebframework.web.entity.organizational;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

/**
 * @author zhouhao
 */
@Getter
@Setter
public class SimplePersonAuthBindEntity extends SimplePersonEntity implements PersonAuthBindEntity {
    private PersonUserEntity personUser;
    //职务ID集合
    private Set<String>      positionIds;

}
