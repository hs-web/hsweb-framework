package org.hswebframework.web.dashboard;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.DataStatusEnum;
import org.hswebframework.web.commons.entity.RecordCreationEntity;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

@EqualsAndHashCode(callSuper = true)
@Data
public class DashBoardConfigEntity extends SimpleGenericEntity<String> implements RecordCreationEntity {

    private static final long serialVersionUID = 3911748291957287662L;

    @NotBlank(groups = CreateGroup.class)
    private String name;

    @NotBlank(groups = CreateGroup.class)
    private String type;

    private String template;

    private String script;

    private String scriptLanguage;

    private String permission;

    private Boolean defaultConfig;

    private DataStatusEnum status;

    @NotBlank(groups = CreateGroup.class)
    private String creatorId;

    @NotBlank(groups = CreateGroup.class)
    private Long createTime;

}
