package org.hswebframework.web.workflow.web.response;

import lombok.Data;
import org.activiti.bpmn.model.Activity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.hswebframework.web.commons.bean.Bean;

import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Data
public class ActivityInfo implements Bean {
    private static final long serialVersionUID = -3273391092933844118L;
    private String id;

    private int x      = -1;
    private int y      = -1;
    private int width  = -1;
    private int height = -1;

    private String processDefinitionId;
    private String processDefinitionKey;
    private String processDefinitionName;

    private Map<String, Object> properties;

    public static ActivityInfo of(ActivityImpl activity) {
        ActivityInfo info = new ActivityInfo();
        info.copyFrom(activity);
        info.setProcessDefinitionId(activity.getProcessDefinition().getId());
        info.setProcessDefinitionKey(activity.getProcessDefinition().getKey());
        info.setProcessDefinitionName(activity.getProcessDefinition().getName());
        return info;
    }
}
