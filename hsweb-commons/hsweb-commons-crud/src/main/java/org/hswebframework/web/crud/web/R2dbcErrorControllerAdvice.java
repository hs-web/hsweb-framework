package org.hswebframework.web.crud.web;

import io.r2dbc.spi.R2dbcException;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.i18n.LocaleUtils;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

/**
 * 统一r2dbc错误处理
 *
 * @author zhouhao
 * @since 4.0
 */
@RestControllerAdvice
@Slf4j
@Order
public class R2dbcErrorControllerAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseMessage<Object>> handleException(R2dbcException e) {
        log.error(e.getLocalizedMessage(), e);
        return LocaleUtils
                .resolveMessageReactive("error.internal_server_error")
                .map(msg -> ResponseMessage.error(500, "error." + e.getClass().getSimpleName(), msg));
    }

}
