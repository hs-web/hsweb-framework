package org.hswebframework.web.entity.authorization;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

import java.util.Date;

/**
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
@NoArgsConstructor
public class UserSettingEntity extends SimpleGenericEntity<String> {
    @NotBlank(groups = CreateGroup.class)
    private String userId;

    @NotBlank(groups = CreateGroup.class)

    private String key;

    @NotBlank(groups = CreateGroup.class)
    private String settingId;

    @NotBlank(groups = CreateGroup.class)
    private String setting;

    private String describe;

    private String name;

    private Date createTime;

    private Date updateTime;

}
