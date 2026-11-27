package org.hswebframework.web.bean;

import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.exception.I18nSupportException;
import org.hswebframework.web.exception.NotFoundException;
import org.hswebframework.web.exception.RemoteException;
import org.hswebframework.web.exception.RemoteExceptionMetadata;
import org.hswebframework.web.exception.RemoteExceptionMetadata.Description;
import org.hswebframework.web.exception.ValidationException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 异常描述与受控还原，不对异常的任意 getter 进行内省，也不按远端 type 加载类。
 * 已知平台异常还原为对应 NoStackTrace，未知类型使用携带远端描述的安全包装。
 */
public class DefaultExceptionConverter implements ExceptionConverter {

    @Override
    public void toMap(Throwable source, BiConsumer<String, Supplier<?>> properties) {
        properties.accept("type", () -> source instanceof RemoteExceptionMetadata remote
            ? remote.getRemoteType() : source.getClass().getName());
        properties.accept("message", source::getMessage);
        properties.accept("detail", () -> source instanceof RemoteExceptionMetadata remote
            ? remote.getRemoteDetail() : getDetail(source));
        if (source instanceof RemoteExceptionMetadata remote) {
            properties.accept("i18nCode", () -> remote.getRemoteDescription().i18nCode());
            properties.accept("args", () -> remote.getRemoteDescription().args());
            properties.accept("code", () -> remote.getRemoteDescription().code());
            properties.accept("status", () -> remote.getRemoteDescription().status());
            properties.accept("details", () -> describeDetails(source instanceof ValidationException validation
                ? validation.getDetails() : remote.getRemoteDescription().details()));
            return;
        }
        if (source instanceof I18nSupportException i18n) {
            properties.accept("i18nCode", i18n::getI18nCode);
            properties.accept("args", i18n::getArgs);
        }
        if (source instanceof BusinessException business) {
            properties.accept("code", business::getCode);
            properties.accept("status", business::getStatus);
        }
        if (source instanceof ValidationException validation) {
            properties.accept("details", () -> describeDetails(validation.getDetails()));
        }
    }

    @Override
    public Throwable fromMap(Map<String, ?> source, Class<? extends Throwable> targetType) {
        Description description = new Description(
            stringValue(source, "type"), stringValue(source, "message"), stringValue(source, "detail"),
            stringValue(source, "i18nCode"), arguments(source.get("args")), stringValue(source, "code"),
            status(source.get("status")), details(source.get("details")));
        String type = description.type();
        Throwable restored;
        if (isType(type, NotFoundException.class, NotFoundException.NoStackTrace.class)) {
            restored = new RemoteException.NotFound(description);
        } else if (isType(type, ValidationException.class, ValidationException.NoStackTrace.class)) {
            restored = new RemoteException.Validation(description);
        } else if (isType(type, I18nSupportException.class, I18nSupportException.NoStackTrace.class)) {
            restored = new RemoteException.I18n(description);
        } else {
            // BusinessException 的 NoStackTrace 同时作为未知类型的安全包装。
            restored = new RemoteException(description);
        }
        if (restored instanceof BusinessException business
            && (!Objects.equals(description.code(), business.getCode())
                || description.status() != null && description.status() != business.getStatus())) {
            throw new IllegalArgumentException("Exception business fields do not match type: " + type);
        }
        if (!targetType.isInstance(restored)) {
            throw new UnsupportedOperationException("Unsupported exception target: " + targetType.getName());
        }
        return restored;
    }

    private boolean isType(String type, Class<?> normal, Class<?> stackless) {
        return normal.getName().equals(type) || stackless.getName().equals(type);
    }

    private String stringValue(Map<?, ?> source, String name) {
        Object value = source.get(name);
        if (value == null || value instanceof String) {
            return (String) value;
        }
        throw new IllegalArgumentException("Exception field must be a string: " + name);
    }

    private Object[] arguments(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Object[] args) {
            return args.clone();
        }
        if (value instanceof List<?> args) {
            return args.toArray();
        }
        throw new IllegalArgumentException("Exception args must be an array or list");
    }

    private Integer status(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            try {
                return new BigDecimal(value.toString()).intValueExact();
            } catch (ArithmeticException | NumberFormatException e) {
                throw new IllegalArgumentException("Exception status must be an int", e);
            }
        }
        throw new IllegalArgumentException("Exception status must be an int");
    }

    private List<Map<String, Object>> describeDetails(List<ValidationException.Detail> details) {
        if (details == null) {
            return null;
        }
        List<Map<String, Object>> result = new ArrayList<>(details.size());
        for (ValidationException.Detail detail : details) {
            Map<String, Object> fields = new LinkedHashMap<>();
            fields.put("property", detail.getProperty());
            fields.put("message", detail.getMessage());
            fields.put("detail", detail.getDetail());
            result.add(fields);
        }
        return result;
    }

    private List<ValidationException.Detail> details(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException("Exception details must be a list");
        }
        List<ValidationException.Detail> result = new ArrayList<>(list.size());
        for (Object item : list) {
            if (item instanceof ValidationException.Detail detail) {
                result.add(new ValidationException.Detail(detail.getProperty(), detail.getMessage(), detail.getDetail()));
            } else if (item instanceof Map<?, ?> fields) {
                result.add(new ValidationException.Detail(
                    stringValue(fields, "property"), stringValue(fields, "message"), fields.get("detail")));
            } else {
                throw new IllegalArgumentException("Exception detail must be an object");
            }
        }
        return result;
    }

    /**
     * 为原始异常生成诊断，可覆盖以使用上层平台的栈精简能力；远端描述直接沿用。
     */
    protected String getDetail(Throwable source) {
        StringWriter writer = new StringWriter();
        source.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
