package org.hswebframework.web.workflow.web.diagram;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@RestController
@RequestMapping("/workflow/service/")
public class ProcessDefinitionDiagramLayoutResource
        extends BaseProcessDefinitionDiagramLayoutResource {

    @GetMapping(
            value = {"/process-definition/{processDefinitionId}/diagram-layout"},
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE
    )
    public Object getDiagram(@PathVariable String processDefinitionId) {
        return this.getDiagramNode(null, processDefinitionId);
    }
}
