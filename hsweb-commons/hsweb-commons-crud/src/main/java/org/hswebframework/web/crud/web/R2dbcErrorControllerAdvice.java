package org.hswebframework.web.crud.web;

import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.i18n.LocaleUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@Slf4j
@RestControllerAdvice
public class R2dbcErrorControllerAdvice {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<Object>> handleException(R2dbcDataIntegrityViolationException e) {
        String code;

        if (e.getMessage().contains("Duplicate")) {
            code = "error.duplicate_data";
        } else {
            code = "error.data_error";
            log.warn(e.getMessage(), e);
        }
        return LocaleUtils
                .resolveMessageReactive(code)
                .map(msg -> ResponseMessage.error(400, code, msg));
    }
}
