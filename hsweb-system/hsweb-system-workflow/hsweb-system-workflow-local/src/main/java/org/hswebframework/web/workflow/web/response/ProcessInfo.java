package org.hswebframework.web.workflow.web.response;

import lombok.Data;
import org.activiti.engine.runtime.ProcessInstance;
import org.hswebframework.web.commons.bean.Bean;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Data
public class ProcessInfo implements Bean {

    private static final long serialVersionUID = 6903869441158752614L;

    private String id;

    private String processDefinitionId;

    private String processDefinitionKey;

    private String processDefinitionName;

    private Integer processDefinitionVersion;

    private String deploymentId;

    private String activityId;

    private String activityName;

    private String processInstanceId;

    private String businessKey;

    private String parentId;

    private boolean suspended;

    public static ProcessInfo of(ProcessInstance processInstance) {
        ProcessInfo info = new ProcessInfo();
        info.copyFrom(processInstance);
        info.suspended = processInstance.isSuspended();
        return info;
    }
}
