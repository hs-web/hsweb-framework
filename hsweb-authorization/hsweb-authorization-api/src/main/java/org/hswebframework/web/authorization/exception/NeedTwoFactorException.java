package org.hswebframework.web.authorization.exception;

import lombok.Getter;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@Getter
public class NeedTwoFactorException extends RuntimeException {
    private static final long   serialVersionUID = 3655980280834947633L;
    private              String provider;

    public NeedTwoFactorException(String message, String provider) {
        super(message);
        this.provider = provider;
    }

}
