package org.hswebframework.web.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.i18n.LocaleUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.ConstraintViolation;
import java.util.*;
import java.util.stream.Collectors;

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
        super(message, args);
        this.details = details;
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
        String propertyI18n = propertyI18nEnabled ?
                first.getRootBeanClass().getName() + "." + property
                : property;

        setArgs(new Object[]{propertyI18n, first.getMessage()});

        details = new ArrayList<>(violations.size());
        for (ConstraintViolation<?> violation : violations) {
            details.add(new Detail(violation.getPropertyPath().toString(),
                                   violation.getMessage(),
                                   null));
        }
    }

    public List<Detail> getDetails(Locale locale) {
        return CollectionUtils.isEmpty(details)
                ? Collections.emptyList()
                : details
                .stream()
                .map(detail -> detail.translateI18n(locale))
                .collect(Collectors.toList());
    }

    @Override
    public String getLocalizedMessage(Locale locale) {
        if (propertyI18nEnabled && "validation.property_validate_failed".equals(getI18nCode()) && getArgs().length > 0) {
            Object[] args = getArgs().clone();
            args[0] = LocaleUtils.resolveMessage(String.valueOf(args[0]), locale, String.valueOf(args[0]));
            return LocaleUtils.resolveMessage(getI18nCode(), locale, getMessage(), args);
        }
        return super.getLocalizedMessage(locale);
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

        public Detail translateI18n(Locale locale) {
            if (StringUtils.hasText(message) && message.contains(".")) {
                return new Detail(property, LocaleUtils.resolveMessage(message, locale, message), detail);
            }
            return this;
        }
    }
}
