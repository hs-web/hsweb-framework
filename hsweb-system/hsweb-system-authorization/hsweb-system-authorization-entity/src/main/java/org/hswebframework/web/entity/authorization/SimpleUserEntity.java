package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
public class SimpleUserEntity extends SimpleGenericEntity<String> implements UserEntity {
    private static final long serialVersionUID = -2625681326256009807L;

    private String name;

    private String username;

    private String password;

    private String salt;

    private Long createTime;

    private String creatorId;

    private Byte status;

    @Override
    public SimpleUserEntity clone() {
        return ((SimpleUserEntity) super.clone());
    }
}
