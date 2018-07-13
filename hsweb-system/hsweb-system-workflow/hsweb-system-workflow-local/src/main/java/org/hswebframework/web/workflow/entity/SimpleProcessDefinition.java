package org.hswebframework.web.workflow.entity;

import lombok.Getter;
import lombok.Setter;
import org.activiti.engine.repository.ProcessDefinition;
import org.hswebframework.web.commons.bean.Bean;

/**
 * @author zhouhao
 */
@Getter
@Setter
public class SimpleProcessDefinition implements Bean {

    private static final long serialVersionUID = -7246626050183062980L;

    private String  id;
    private String  category;
    private String  name;
    private String  key;
    private String  description;
    private int     version;
    private String  resourceName;
    private String  deploymentId;
    private String  diagramResourceName;
    private boolean suspended;
    private boolean hasStartFormKey;
    private boolean hasGraphicalNotation;
    private String  tenantId;

    public SimpleProcessDefinition() {
    }

    public SimpleProcessDefinition(ProcessDefinition definition) {
        id = definition.getId();
        category = definition.getCategory();
        name = definition.getName();
        key = definition.getKey();
        description = definition.getDescription();
        version = definition.getVersion();

        resourceName = definition.getResourceName();
        deploymentId = definition.getDeploymentId();
        diagramResourceName = definition.getResourceName();

        suspended = definition.isSuspended();

        hasGraphicalNotation = definition.hasGraphicalNotation();
        hasStartFormKey = definition.hasStartFormKey();
        tenantId = definition.getTenantId();
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

}
