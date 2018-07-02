package org.hswebframework.web.service.form.simple.validator.jsr303;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
@Slf4j
public class NotNullStrategy extends AbstractStrategy {

    @Override
    protected Class<NotNull> getAnnotationType() {
        return NotNull.class;
    }
}
