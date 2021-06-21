package org.hswebframework.web.exception;


import lombok.Getter;

@Getter
public class I18nSupportException extends RuntimeException {

    private final Object[] args;

    public I18nSupportException(String code, Object... args) {
        super(code);
        this.args = args;
    }

    public I18nSupportException(String code, Throwable cause, Object... args) {
        super(code, cause);
        this.args = args;
    }


    @Override
    public String getLocalizedMessage() {
        // TODO: 2021/6/21
        return super.getLocalizedMessage();
    }
}
