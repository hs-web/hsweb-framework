package org.hswebframework.web.exception;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.i18n.LocaleUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Locale;

/**
 * 支持国际化消息的异常,code为
 *
 * @author zhouhao
 * @see LocaleUtils#resolveMessage(String, Object...)
 * @since 4.0.11
 */
@Getter
@Setter(AccessLevel.PROTECTED)
public class I18nSupportException extends TraceSourceException {

    /**
     * 消息code,在message.properties文件中定义的key
     */
    private String i18nCode;

    /**
     * 消息参数
     */
    private Object[] args;

    protected I18nSupportException() {

    }

    public I18nSupportException(String code, Object... args) {
        super(code);
        this.i18nCode = code;
        this.args = args;
    }

    public I18nSupportException(String code, Throwable cause, Object... args) {
        super(code, cause);
        this.args = args;
        this.i18nCode = code;
    }

    public String getOriginalMessage() {
        return super.getMessage() != null ? super.getMessage() : getI18nCode();
    }

    @Override
    public String getMessage() {
        return getLocalizedMessage();
    }

    @Override
    public final String getLocalizedMessage() {
        return getLocalizedMessage(LocaleUtils.current());
    }

    public String getLocalizedMessage(Locale locale) {
        return LocaleUtils.resolveMessage(i18nCode, locale, getOriginalMessage(), args);
    }

    public final Mono<String> getLocalizedMessageReactive() {
        return LocaleUtils
            .currentReactive()
            .map(this::getLocalizedMessage);
    }

    public static String tryGetLocalizedMessage(Throwable error, Locale locale) {
        if (error instanceof I18nSupportException) {
            return ((I18nSupportException) error).getLocalizedMessage(locale);
        }
        String msg = error.getMessage();

        if (!StringUtils.hasText(msg)) {
            msg = "error." + error.getClass().getSimpleName();
        }
        if (msg.contains(".")) {
            return LocaleUtils.resolveMessage(msg, locale, msg);
        }
        return msg;
    }

    public static String tryGetLocalizedMessage(Throwable error) {
        return tryGetLocalizedMessage(error, LocaleUtils.current());
    }

    public static Mono<String> tryGetLocalizedMessageReactive(Throwable error) {
        return LocaleUtils
            .currentReactive()
            .map(locale -> tryGetLocalizedMessage(error, locale));
    }

    /**
     * 不填充线程栈的异常，在一些对线程栈不敏感，且对异常不可控（如: 来自未认证请求产生的异常）的情况下不填充线程栈对性能有利。
     */
    public static class NoStackTrace extends I18nSupportException {
        public NoStackTrace(String code, Object... args) {
            super(code, args);
        }

        public NoStackTrace(String code, Throwable cause, Object... args) {
            super(code, cause, args);
        }

        @Override
        public final synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
