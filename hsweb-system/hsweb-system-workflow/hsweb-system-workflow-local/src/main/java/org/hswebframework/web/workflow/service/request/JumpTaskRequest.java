package org.hswebframework.web.workflow.service.request;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.bean.ValidateBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JumpTaskRequest implements ValidateBean {
    private static final long serialVersionUID = 7625759475416169067L;

    @NotBlank(message = "[jumpUserId]不能为空")
    private String jumpUserId;

    @NotBlank(message = "[jumpUserName]不能为空")
    private String jumpUserName;

    @NotBlank(message = "[taskId]不能为空")
    private String taskId;

    @NotBlank(message = "[targetActivityId]不能为空")
    private String targetActivityId;

    //是否记录到流程日志
    private boolean recordLog = true;

    //自定义数据,将会记录到流程历史记录里,比如回退原因等
    private Map<String, Object> data = new HashMap<>();
}
