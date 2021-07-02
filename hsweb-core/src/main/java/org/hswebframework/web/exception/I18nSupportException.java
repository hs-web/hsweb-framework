package org.hswebframework.web.exception;


import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.i18n.LocaleUtils;

@Getter
@Setter(AccessLevel.PROTECTED)
public class I18nSupportException extends RuntimeException {
    private String code;
    private Object[] args;

    protected I18nSupportException() {

    }

    public I18nSupportException(String code, Object... args) {
        super(code);
        this.code = code;
        this.args = args;
    }

    public I18nSupportException(String code, Throwable cause, Object... args) {
        super(code, cause);
        this.args = args;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return code;
    }

    @Override
    public String getLocalizedMessage() {
        return LocaleUtils.resolveMessage(code, args);
    }
}
