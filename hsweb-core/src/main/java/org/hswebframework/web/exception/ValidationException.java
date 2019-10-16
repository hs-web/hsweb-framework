package org.hswebframework.web.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class ValidationException extends BusinessException {

    private List<Detail> details;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, List<Detail> details) {
        super(message);
        this.details = details;
    }

    public ValidationException(String message, Set<? extends ConstraintViolation> violations) {
        super(message);
        if (null != violations && !violations.isEmpty()) {
            details = new ArrayList<>();
            for (ConstraintViolation<?> violation : violations) {
                details.add(new Detail(violation.getPropertyPath().toString(), violation.getMessage(), null));
            }
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Detail {
        String property;

        String message;

        Object detail;
    }
}
