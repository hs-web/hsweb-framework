package org.hswebframework.web.workflow.service.request;

import lombok.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.commons.bean.ValidateBean;

import java.util.HashMap;
import java.util.Map;

/**
 * 启动流程请求
 *
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StartProcessRequest implements ValidateBean {
    private static final long serialVersionUID = -344629653235864995L;
    /**
     * 流程定义ID
     *
     * @see ProcessDefinition#getId()
     */
    @NotBlank
    private String processDefineId;

    /**
     * 流程发起人ID
     *
     * @see Authentication#current()
     * @see Authentication#getUser()
     * @see User#getId()
     */
    @NotBlank
    private String creatorId;

    /**
     * 流程发起人姓名
     *
     * @see User#getName()
     */
    @NotBlank
    private String creatorName;

    /**
     * 下一环节的办理人ID
     */
    private String nextClaimUserId;

    /**
     * 流程变量
     */
    private Map<String, Object> variables = new HashMap<>();

    /**
     * 表单数据
     */
    private Map<String, Object> formData = new HashMap<>();
}
