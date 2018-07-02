package org.hswebframework.web.service.form.simple.validator.jsr303;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.stereotype.Component;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
@Slf4j
public class RangeStrategy extends AbstractStrategy {

    public RangeStrategy() {
        addPropertyMapping(PropertyMapping.of("min", int.class));

        addPropertyMapping(PropertyMapping.of("max", int.class));
    }

    @Override
    protected Class<Range> getAnnotationType() {
        return Range.class;
    }
}
