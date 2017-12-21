package org.hswebframework.web.example.custom.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hswebframework.web.entity.authorization.bind.SimpleBindRoleUserEntity;

import javax.persistence.Column;
import javax.persistence.Table;

/**
 * 使用jpa注解的方式来拓展字段信息
 *
 * @author zhouhao
 * @since 3.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table
public class CustomUserEntity extends SimpleBindRoleUserEntity {
    @Column(name = "nick_name")
    private String nickName;
}
