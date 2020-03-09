package org.hswebframework.web.system.authorization.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.DefaultValue;
import org.hswebframework.web.api.crud.entity.GenericEntity;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.bean.ToString;
import org.hswebframework.web.validator.CreateGroup;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Table(name = "s_user",
        indexes = @Index(name = "user_username_idx", columnList = "username", unique = true)
)
public class UserEntity extends GenericEntity<String> implements RecordCreationEntity {

    @Column(length = 128, nullable = false)
    @NotBlank(message = "姓名不能为空", groups = CreateGroup.class)
    private String name;

    @Column(length = 128, nullable = false, updatable = false)
    @NotBlank(message = "用户名不能为空", groups = CreateGroup.class)
    private String username;

    @Column(nullable = false)
    @ToString.Ignore(cover = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "密码不能为空", groups = CreateGroup.class)
    private String password;

    @Column(nullable = false)
    @ToString.Ignore(cover = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String salt;

    @Column
    private String type;

    @Column
    @DefaultValue("1")
    private Byte status;

    @Column(name = "creator_id", updatable = false)
    private String creatorId;

    @Column(name = "create_time", updatable = false)
    @DefaultValue(generator = "current_time")
    private Long createTime;

    @Override
    public String getId() {
        return super.getId();
    }
}
