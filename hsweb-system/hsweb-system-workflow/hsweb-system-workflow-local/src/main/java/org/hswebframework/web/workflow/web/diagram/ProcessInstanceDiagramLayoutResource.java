package org.hswebframework.web.workflow.web.diagram;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@RestController
@RequestMapping("/workflow/service/")
public class ProcessInstanceDiagramLayoutResource
        extends BaseProcessDefinitionDiagramLayoutResource
{


    @GetMapping(
            value = {"/process-instance/{processInstanceId}/diagram-layout"},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public Object getDiagram(@PathVariable String processInstanceId) {
        return this.getDiagramNode(processInstanceId, null);
    }
}
