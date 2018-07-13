package org.hswebframework.web.workflow.web.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.activiti.engine.repository.ProcessDefinition;
import org.hswebframework.web.commons.bean.Bean;

/**
 * @author zhouhao
 */
@Data
public class ProcessDefinitionInfo implements Bean {

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

    public static ProcessDefinitionInfo of(ProcessDefinition definition) {
        ProcessDefinitionInfo info = new ProcessDefinitionInfo();
        info.copyFrom(definition);

        info.hasGraphicalNotation = definition.hasGraphicalNotation();
        info.hasStartFormKey = definition.hasStartFormKey();
        return info;
    }

}
