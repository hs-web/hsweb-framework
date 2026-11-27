package org.hswebframework.web.bean;

import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.exception.I18nSupportException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * 提供异常描述及国际化信息，不对异常的任意 getter 进行内省。
 * detail 使用完整堆栈文本，避免在 Map 中保留 cause/suppressed 对象图。
 */
public class DefaultExceptionToMapConverter implements ExceptionToMapConverter {

    @Override
    public void convert(Throwable source, BiConsumer<String, Supplier<?>> properties) {
        properties.accept("type", () -> source.getClass().getName());
        properties.accept("message", source::getMessage);
        properties.accept("detail", () -> getDetail(source));
        if (source instanceof I18nSupportException i18n) {
            properties.accept("i18nCode", i18n::getI18nCode);
            properties.accept("args", i18n::getArgs);
        }
        if (source instanceof BusinessException business) {
            properties.accept("code", business::getCode);
            properties.accept("status", business::getStatus);
        }
    }

    /**
     * 覆盖此方法可使用上层平台的栈精简能力。
     */
    protected String getDetail(Throwable source) {
        StringWriter writer = new StringWriter();
        source.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }
}
