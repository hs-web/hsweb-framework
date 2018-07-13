package org.hswebframework.web.workflow.web.diagram;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@RestController
@RequestMapping("/workflow/service/")
public class ProcessInstanceDiagramLayoutResource
        extends BaseProcessDefinitionDiagramLayoutResource
{


    @RequestMapping(
            value = {"/process-instance/{processInstanceId}/diagram-layout"},
            method = {RequestMethod.GET},
            produces = {"application/json"}
    )
    public Object getDiagram(@PathVariable String processInstanceId) {
        return this.getDiagramNode(processInstanceId, (String)null);
    }
}
