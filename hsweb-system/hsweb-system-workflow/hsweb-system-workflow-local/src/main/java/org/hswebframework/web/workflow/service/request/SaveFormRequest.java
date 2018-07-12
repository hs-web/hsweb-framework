package org.hswebframework.web.workflow.service.request;

import lombok.*;
import org.hibernate.validator.constraints.NotBlank;
import org.hswebframework.web.commons.bean.ValidateBean;

import javax.validation.constraints.NotNull;
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
public class SaveFormRequest implements ValidateBean {
    private static final long serialVersionUID = 7575220908978610735L;

    @NotBlank
    private String userId;

    @NotBlank
    private String userName;

    private Map<String, Object> formData = new HashMap<>();
}
