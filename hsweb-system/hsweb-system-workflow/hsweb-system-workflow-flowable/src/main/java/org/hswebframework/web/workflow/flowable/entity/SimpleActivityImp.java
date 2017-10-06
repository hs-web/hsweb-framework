package org.hswebframework.web.workflow.flowable.entity;

import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;

/**
 * @Author wangwei
 * @Date 2017/9/4.
 */
public class SimpleActivityImp extends ActivityImpl {

    public SimpleActivityImp(String id, ProcessDefinitionImpl processDefinition) {
        super(id, processDefinition);
    }
}
