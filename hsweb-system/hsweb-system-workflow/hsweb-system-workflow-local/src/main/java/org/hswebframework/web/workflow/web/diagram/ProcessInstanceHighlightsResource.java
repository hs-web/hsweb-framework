package org.hswebframework.web.workflow.web.diagram;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@RestController
@RequestMapping("/workflow/service/")
public class ProcessInstanceHighlightsResource extends org.activiti.rest.diagram.services.ProcessInstanceHighlightsResource {
}
