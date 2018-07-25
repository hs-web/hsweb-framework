package org.hswebframework.web.workflow.service.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.bean.Bean;
import org.hswebframework.web.commons.bean.ValidateBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectTaskRequest implements ValidateBean {
    private static final long serialVersionUID = 7625759475416169067L;

    @NotBlank(message = "[rejectUserId]不能为空")
    private String rejectUserId;

    @NotBlank(message = "[rejectUserName]不能为空")
    private String rejectUserName;

    @NotBlank(message = "[processInstanceId]不能为空")
    private String processInstanceId;

    @NotBlank(message = "[activityId]不能为空")
    private String activityId;

    //自定义数据,将会记录到流程历史记录里,比如回退原因等
    private Map<String, Object> data = new HashMap<>();
}
