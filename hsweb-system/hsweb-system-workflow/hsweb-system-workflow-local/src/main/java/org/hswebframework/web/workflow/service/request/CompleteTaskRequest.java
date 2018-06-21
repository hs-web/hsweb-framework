package org.hswebframework.web.workflow.service.request;

import lombok.*;

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
public class CompleteTaskRequest {
    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 完成此任务的用户ID
     */
    private String completeUserId;

    /**
     * 完成此任务的用户姓名
     */
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
    private Map<String, Object> variables;

    /**
     * 表单数据
     */
    private Map<String, Object> formData;
}
