package org.hswebframework.web.service.form.simple.validator.jsr303;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Pattern;
import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
@Component
@Slf4j
public class EmailStrategy extends AbstractStrategy {

    public EmailStrategy() {
        addPropertyMapping(PropertyMapping.of("regexp", String.class));
    }

    @Override
    protected Class<Email> getAnnotationType() {
        return Email.class;
    }
}
