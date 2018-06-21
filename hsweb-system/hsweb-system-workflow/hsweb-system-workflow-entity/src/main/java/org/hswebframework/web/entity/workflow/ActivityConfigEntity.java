package org.hswebframework.web.entity.workflow;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

@Getter
@Setter
public class ActivityConfigEntity extends SimpleGenericEntity<String> {

    /**
     * 节点ID
     */
    @NotBlank(groups = CreateGroup.class)
    private String activityId;

    /**
     * 流程定义ID
     */
    @NotBlank(groups = CreateGroup.class)
    private String processDefineId;

    /**
     * 后台表单ID
     */
    private String formId;

    /**
     * 前端表单模版ID
     */
    private String formTemplateId;

    /**
     * 节点办理候选人维度,用于设置该环节的办理人,json格式,由CandidateDimensionParser解析
     */
    private String candidateDimension;

    /**
     * 版本号
     */
    private Long version;


}
