package org.hswebframework.web.workflow.dao.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;
import org.hswebframework.web.validator.group.CreateGroup;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ActivityConfigEntity extends SimpleGenericEntity<String> {

    private static final long serialVersionUID = 2909584456889604626L;

    /**
     * 节点ID
     */
    @NotBlank(groups = CreateGroup.class)
    private String activityId;

    /**
     * 流程定义key
     */
    @NotBlank(groups = CreateGroup.class)
    private String processDefineKey;

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
     * 节点办理候选人维度,用于设置该环节的办理人,json格式,由CandidateDimensionParser解析
     */
    private String candidateDimension;

    @NotNull(groups = CreateGroup.class)
    private Date createTime;

    private Date updateTime;

    @NotNull(groups = CreateGroup.class)
    private Byte status;

    private List<ListenerConfig> listeners;

}
