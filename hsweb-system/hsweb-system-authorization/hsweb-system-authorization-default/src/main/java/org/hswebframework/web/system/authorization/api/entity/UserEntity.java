package org.hswebframework.web.system.authorization.api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.bean.ToString;

import javax.persistence.Column;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Table(name = "s_user", indexes =
@Index(name = "user_username_idx", columnList = "username", unique = true)
)
public class UserEntity  extends GenericEntity<String> implements RecordCreationEntity {

    @Column(length = 128, nullable = false)
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Column(length = 128, nullable = false, updatable = false)
    private String username;

    @Column(nullable = false)
    @ToString.Ignore(cover = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    @ToString.Ignore(cover = false)
    @JsonIgnore
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
