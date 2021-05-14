package org.hswebframework.web.system.authorization.api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
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

/**
 * 系统用户实体
 *
 * @author zhouhao
 * @see org.hswebframework.web.system.authorization.api.event.UserDeletedEvent
 * @see org.hswebframework.web.system.authorization.api.event.UserCreatedEvent
 * @see org.hswebframework.web.system.authorization.api.event.UserModifiedEvent
 * @since 4.0.0
 */
@Getter
@Setter
@Table(name = "s_user",
        indexes = @Index(name = "user_username_idx", columnList = "username", unique = true)
)
public class UserEntity extends GenericEntity<String> implements RecordCreationEntity {

    @Column(length = 128, nullable = false)
    @NotBlank(message = "姓名不能为空", groups = CreateGroup.class)
    @Schema(description = "姓名")
    private String name;

    @Column(length = 128, nullable = false, updatable = false)
    @NotBlank(message = "用户名不能为空", groups = CreateGroup.class)
    @Schema(description = "用户名")
    private String username;

    @Column(nullable = false)
    @ToString.Ignore(cover = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "密码不能为空", groups = CreateGroup.class)
    @Schema(description = "密码")
    private String password;

    @Column(nullable = false)
    @ToString.Ignore(cover = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Hidden
    private String salt;

    @Column
    @Schema(description = "用户类型")
    private String type;

    @Column
    @DefaultValue("1")
    @Schema(description = "用户状态")
    private Byte status;

    @Column(name = "creator_id", updatable = false)
    @Hidden
    private String creatorId;

    @Column(name = "create_time", updatable = false)
    @DefaultValue(generator = "current_time")
    @Hidden
    private Long createTime;

    @Override
    public String getId() {
        return super.getId();
    }
}
