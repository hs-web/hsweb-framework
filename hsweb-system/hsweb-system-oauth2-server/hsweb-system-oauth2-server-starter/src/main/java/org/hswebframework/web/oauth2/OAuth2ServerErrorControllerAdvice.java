package org.hswebframework.web.oauth2;

import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RestControllerAdvice
public class OAuth2ServerErrorControllerAdvice {

    @ExceptionHandler(GrantTokenException.class)
    @ResponseStatus(HttpStatus.OK)
    public ResponseMessage<String> error(GrantTokenException e) {
        return ResponseMessage.error(e.getErrorType().code(), e.getErrorType().message());
    }
}
