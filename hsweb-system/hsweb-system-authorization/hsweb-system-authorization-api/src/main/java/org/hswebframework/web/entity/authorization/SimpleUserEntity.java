package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.web.bean.ToString;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
@Table(name = "s_user", indexes = {
        @Index(name = "idx_user_name", columnList = "username", unique = true)
})
public class SimpleUserEntity extends SimpleGenericEntity<String> implements UserEntity {
    private static final long serialVersionUID = -2625681326256009807L;
    @Column
    @Comment("姓名")
    private String name;

    @Column
    @Comment("用户名")
    private String username;

    @ToString.Ignore
    @Column
    @Comment("密码")
    private String password;

    @ToString.Ignore(cover = false)
    @Column
    @Comment("密码盐")
    private String salt;

    @Column(name = "create_time")
    private Long createTime;

    @Column(name = "creator_id")
    private String creatorId;

    @Column
    private Byte status;

    @Override
    public SimpleUserEntity clone() {
        return ((SimpleUserEntity) super.clone());
    }

    @Override
    @Id
    @Column(name = "u_id")
    public String getId() {
        return super.getId();
    }
}
