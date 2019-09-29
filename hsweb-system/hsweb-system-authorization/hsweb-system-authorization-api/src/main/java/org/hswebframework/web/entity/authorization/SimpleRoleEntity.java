package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
@Table(name = "s_role")
public class SimpleRoleEntity extends SimpleGenericEntity<String> implements RoleEntity {
    private static final long serialVersionUID = -2857131363164004807L;

    @Column
    private String name;

    @Column
    private String describe;

    @Column
    private Byte status;

    @Override
    public SimpleRoleEntity clone() {
        return ((SimpleRoleEntity) super.clone());
    }
}
