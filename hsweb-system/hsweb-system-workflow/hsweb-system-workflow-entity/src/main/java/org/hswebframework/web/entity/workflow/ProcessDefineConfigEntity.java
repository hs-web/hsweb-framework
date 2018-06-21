package org.hswebframework.web.entity.workflow;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

@Getter
@Setter
public class ProcessDefineConfigEntity extends SimpleGenericEntity<String> {

    /**
     * 流程定义ID
     */
    @NotBlank(groups = CreateGroup.class)
    private String processDefineId;

    /**
     * 后台表单ID
     */
    @NotBlank(groups = CreateGroup.class)
    private String formId;

    /**
     * 前端表单模版ID
     */
    private String formTemplateId;

    /**
     * 权限维度,用于控制不同人,可发起不同的流程
     */
    private String permissionDimension;

}
