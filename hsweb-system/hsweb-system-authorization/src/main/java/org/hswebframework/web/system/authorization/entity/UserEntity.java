package org.hswebframework.web.system.authorization.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.bean.ToString;
import org.hswebframework.web.crud.entity.Entity;
import org.hswebframework.web.crud.entity.RecordCreationEntity;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Table(name = "s_user", indexes =
@Index(name = "user_username_idx", columnList = "username", unique = true)
)
public class UserEntity implements RecordCreationEntity, Entity {

    @Id
    @Column(length = 32)
    private String id;

    @Column(length = 128, nullable = false)
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Column(length = 128, nullable = false, updatable = false)
    private String username;

    @Column(nullable = false)
    @ToString.Ignore(cover = false)
    private String password;

    @Column(nullable = false)
    @ToString.Ignore(cover = false)
    private String salt;

    @Column
    private String type;

    @Column
    private Byte status;

    @Column(name = "creator_id", updatable = false)
    private String creatorId;

    @Column(name = "create_time", updatable = false)
    private Long createTime;


}
