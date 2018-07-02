package org.hswebframework.web.service.form.simple.validator.jsr303;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
@Slf4j
public class LengthStrategy extends AbstractStrategy {

    public LengthStrategy() {
        addPropertyMapping(PropertyMapping.of("min", int.class));

        addPropertyMapping(PropertyMapping.of("max", int.class));
    }

    @Override
    protected Class<Length> getAnnotationType() {
        return Length.class;
    }
}
