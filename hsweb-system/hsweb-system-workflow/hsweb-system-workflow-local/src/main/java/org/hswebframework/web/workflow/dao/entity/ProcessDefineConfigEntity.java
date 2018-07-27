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
public class ProcessDefineConfigEntity extends SimpleGenericEntity<String> {

    private static final long serialVersionUID = -140312693789656665L;
    /**
     * 流程定义Key
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
    @NotBlank(groups = CreateGroup.class)
    private String formId;

    /**
     * 权限维度,用于控制不同人,可发起不同的流程
     */
    private String permissionDimension;

    private Date createTime;

    private Date updateTime;

    /**
     * 状态
     */
    @NotNull(groups = CreateGroup.class)
    private Byte status;

    private List<ListenerConfig> listeners;

}
