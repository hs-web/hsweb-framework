package org.hswebframework.web.example.custom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hswebframework.web.entity.authorization.bind.SimpleBindRoleUserEntity;

/**
 * @author zhouhao
 * @since 3.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CustomUserEntity extends SimpleBindRoleUserEntity {

    private String nickName;
}
