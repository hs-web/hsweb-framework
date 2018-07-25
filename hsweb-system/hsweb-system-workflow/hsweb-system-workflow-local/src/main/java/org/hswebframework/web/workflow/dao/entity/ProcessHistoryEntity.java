package org.hswebframework.web.workflow.dao.entity;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import javax.validation.constraints.NotNull;
import java.util.Date;
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
public class ProcessHistoryEntity extends SimpleGenericEntity<String> {

    private static final long serialVersionUID = 1413072102870937450L;

    @NotBlank(message = "[type]不能为空")
    private String type;

    @NotBlank(message = "[typeText]不能为空")
    private String typeText;

    @NotBlank(message = "[processDefineId]不能为空")
    private String processDefineId;

    @NotBlank(message = "[processInstanceId]不能为空")
    private String processInstanceId;

    @NotBlank(message = "[businessKey]不能为空")
    private String businessKey;

    @NotNull(message = "[createTime]不能为空")
    private Date createTime;

    @NotBlank(message = "[creatorId]不能为空")
    private String creatorId;

    @NotBlank(message = "[creatorName]不能为空")
    private String creatorName;

    private Map<String, Object> data = new HashMap<>();

    private String taskId;

    private String taskDefineKey;

    private String taskName;
}
