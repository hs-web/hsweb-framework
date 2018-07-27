package org.hswebframework.web.workflow.service.config;

import org.activiti.engine.task.Task;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.workflow.listener.TaskEventListener;

import java.util.List;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ActivityConfiguration {
    /**
     * 此任务需要提交的表单,如果返回值不为空,则说明此任务在完成的时候,需要一起提交表单数据
     *
     * @return 表单ID, 如果未设置返回null
     * @see org.hswebframework.web.service.form.DynamicFormOperationService
     * @see org.hswebframework.web.service.form.DynamicFormService
     */
    String getFormId();

    /**
     * 判断用户是否可以签收此任务
     *
     * @param userId 用户ID {@link User#getId()}
     * @return 是否可以签收
     * @see User
     * @see org.hswebframework.web.authorization.Authentication
     */
    boolean canClaim(Task task, String userId);

    /**
     * 获取此任务的所有候选人信息
     *
     * @return 此任务的所有候选人信息 {@link CandidateInfo}
     */
    List<CandidateInfo> getCandidateInfo(Task task);

    TaskEventListener getTaskListener(String eventType);
}
