package org.hswebframework.web.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.i18n.LocaleUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import java.util.*;

@Getter
@Setter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends I18nSupportException {

    private static final boolean propertyI18nEnabled = Boolean.getBoolean("i18n.validation.property.enabled");

    private List<Detail> details;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String property, String message, Object... args) {
        this(message, Collections.singletonList(new Detail(property, message, null)), args);
    }

    public ValidationException(String message, List<Detail> details, Object... args) {
        super(message, 400, args);
        this.details = details;
        for (Detail detail : this.details) {
            detail.translateI18n(args);
        }
    }

    public ValidationException(Set<? extends ConstraintViolation<?>> violations) {
        ConstraintViolation<?> first = violations.iterator().next();
        if (Objects.equals(first.getMessageTemplate(), first.getMessage())) {
            //模版和消息相同,说明是自定义的message,而不是已经通过i18n获取的.
            setI18nCode(first.getMessage());
        } else {
            setI18nCode("validation.property_validate_failed");
        }
        String property = first.getPropertyPath().toString();

        //{0} 属性 ，{1} 验证消息
        //property也支持国际化?
        String resolveMessage = propertyI18nEnabled ?
                LocaleUtils.resolveMessage(first.getRootBeanClass().getName() + "." + property, property)
                : property;

        setArgs(new Object[]{resolveMessage, first.getMessage()});

        details = new ArrayList<>(violations.size());
        for (ConstraintViolation<?> violation : violations) {
            details.add(new Detail(violation.getPropertyPath().toString(), violation.getMessage(), null));
        }
    }


    @Getter
    @Setter
    @AllArgsConstructor
    public static class Detail {
        @Schema(description = "字段")
        String property;

        @Schema(description = "说明")
        String message;

        @Schema(description = "详情")
        Object detail;

        public void translateI18n(Object... args) {
            if (message.contains(".")) {
                message = LocaleUtils.resolveMessage(message, message, args);
            }
        }
    }
}
