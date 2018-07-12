package org.hswebframework.web.workflow.service.request;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.bean.ValidateBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteTaskRequest implements ValidateBean {
    private static final long serialVersionUID = -2548459805655649449L;
    /**
     * 任务ID
     */
    @NotBlank
    private String taskId;

    /**
     * 完成此任务的用户ID
     */
    @NotBlank
    private String completeUserId;

    /**
     * 完成此任务的用户姓名
     */
    @NotBlank
    private String completeUserName;

    /**
     * 下一环节的ID
     */
    private String nextActivityId;

    /**
     * 下一环节办理的用户ID
     */
    private String nextClaimUserId;

    /**
     * 变量
     */
    private Map<String, Object> variables = new HashMap<>();

    /**
     * 表单数据
     */
    private Map<String, Object> formData = new HashMap<>();
}
