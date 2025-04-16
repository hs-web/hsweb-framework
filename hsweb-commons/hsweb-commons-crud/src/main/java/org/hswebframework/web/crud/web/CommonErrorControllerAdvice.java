package org.hswebframework.web.crud.web;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.CodeConstants;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.exception.AuthenticationException;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.exception.I18nSupportException;
import org.hswebframework.web.exception.NotFoundException;
import org.hswebframework.web.exception.ValidationException;
import org.hswebframework.web.i18n.LocaleUtils;
import org.hswebframework.web.logger.ReactiveLogger;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

import jakarta.validation.ConstraintViolationException;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * 统一错误处理
 *
 * @author zhouhao
 * @since 4.0
 */
@RestControllerAdvice
@Slf4j
@Order
public class CommonErrorControllerAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseMessage<Object>> handleException(NoResourceFoundException e) {
        return LocaleUtils
            .resolveMessageReactive("error.resource_not_found")
            .map(msg -> ResponseMessage.error(404, "error.resource_not_found", msg));
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseMessage<Object>> handleException(TransactionException e) {
        log.warn(e.getLocalizedMessage(), e);
        return LocaleUtils
            .resolveMessageReactive("error.internal_server_error")
            .map(msg -> ResponseMessage.error(500, "error." + e.getClass().getSimpleName(), msg));
    }

    @ExceptionHandler
    public Mono<ResponseEntity<ResponseMessage<Object>>> handleException(BusinessException e) {
        return LocaleUtils
            .resolveThrowable(e,
                              (err, msg) -> ResponseMessage.error(err.getStatus(), err.getCode(), msg))
            .map(msg -> {
                HttpStatus status = HttpStatus.resolve(msg.getStatus());
                return ResponseEntity
                    .status(status == null ? HttpStatus.BAD_REQUEST : status)
                    .body(msg);
            });
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<Object>> handleException(UnsupportedOperationException e) {
        log.warn(e.getLocalizedMessage(), e);
        return LocaleUtils
            .resolveThrowable(e, (err, msg) ->
                (ResponseMessage.error(400, CodeConstants.Error.unsupported, msg)));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ResponseMessage<TokenState>> handleException(UnAuthorizedException e) {
        return LocaleUtils
            .resolveThrowable(e, (err, msg) -> (ResponseMessage
                .<TokenState>error(401, CodeConstants.Error.unauthorized, msg)
                .result(e.getState())));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<ResponseMessage<Object>> handleException(AccessDenyException e) {
        return LocaleUtils
            .resolveThrowable(e, (err, msg) -> ResponseMessage.error(403, e.getCode(), msg))
            ;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseMessage<Object>> handleException(NotFoundException e) {
        return LocaleUtils
            .resolveThrowable(e, (err, msg) -> ResponseMessage.error(404, CodeConstants.Error.not_found, msg))
            ;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<List<ValidationException.Detail>>> handleException(ValidationException e) {
        return LocaleUtils
            .currentReactive()
            .map(locale -> ResponseMessage
                .<List<ValidationException.Detail>>error(400,
                                                         CodeConstants.Error.illegal_argument,
                                                         e.getLocalizedMessage(locale))
                .result(e.getDetails(locale)));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<List<ValidationException.Detail>>> handleException(ConstraintViolationException e) {
        return handleException(new ValidationException(e.getConstraintViolations()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("all")
    public Mono<ResponseMessage<List<ValidationException.Detail>>> handleException(BindException e) {
        return handleBindingResult(e.getBindingResult());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("all")
    public Mono<ResponseMessage<List<ValidationException.Detail>>> handleException(WebExchangeBindException e) {
        return handleBindingResult(e.getBindingResult());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @SuppressWarnings("all")
    public Mono<ResponseMessage<List<ValidationException.Detail>>> handleException(MethodArgumentNotValidException e) {
        return handleBindingResult(e.getBindingResult());
    }

    private Mono<ResponseMessage<List<ValidationException.Detail>>> handleBindingResult(BindingResult result) {
        String message;
        FieldError fieldError = result.getFieldError();
        ObjectError globalError = result.getGlobalError();

        if (null != fieldError) {
            message = fieldError.getDefaultMessage();
        } else if (null != globalError) {
            message = globalError.getDefaultMessage();
        } else {
            message = CodeConstants.Error.illegal_argument;
        }
        List<ValidationException.Detail> details = result
            .getFieldErrors()
            .stream()
            .map(err -> new ValidationException.Detail(err.getField(), err.getDefaultMessage(), null))
            .collect(Collectors.toList());
        return handleException(new ValidationException(message, details));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<?>> handleException(jakarta.validation.ValidationException e) {
        return Mono.just(ResponseMessage.error(400, CodeConstants.Error.illegal_argument, e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public Mono<ResponseMessage<Object>> handleException(TimeoutException e) {
        return LocaleUtils
            .resolveThrowable(e, (err, msg) -> ResponseMessage.error(504, CodeConstants.Error.timeout, msg))
            .doOnEach(ReactiveLogger.onNext(r -> log.warn(e.getLocalizedMessage(), e)));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order
    public Mono<ResponseMessage<Object>> handleException(RuntimeException e) {
        return LocaleUtils
            .resolveThrowable(e, (err, msg) -> {
                log.warn(msg, e);
                return ResponseMessage.error(msg);
            });
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseMessage<Object>> handleException(NullPointerException e) {
        log.warn(e.getLocalizedMessage(), e);

        return LocaleUtils
            .resolveMessageReactive("error.internal_server_error")
            .map(msg -> ResponseMessage.error(500, "internal_server_error", msg));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<Object>> handleException(IllegalArgumentException e) {

        return LocaleUtils
            .resolveThrowable(e, (err, msg) -> {
                log.warn(msg, e);
                return ResponseMessage.error(400, CodeConstants.Error.illegal_argument, msg);
            });
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<Object>> handleException(AuthenticationException e) {
        return LocaleUtils
            .resolveThrowable(e, (err, msg) -> ResponseMessage.error(400, err.getCode(), msg));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Mono<ResponseMessage<Object>> handleException(UnsupportedMediaTypeStatusException e) {
        log.warn(e.getLocalizedMessage(), e);

        return LocaleUtils
            .resolveMessageReactive("error.unsupported_media_type")
            .map(msg -> ResponseMessage
                .error(415, "unsupported_media_type", msg)
                .result(e.getSupportedMediaTypes()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public Mono<ResponseMessage<Object>> handleException(NotAcceptableStatusException e) {
        log.warn(e.getLocalizedMessage(), e);

        return LocaleUtils
            .resolveMessageReactive("error.not_acceptable_media_type")
            .map(msg -> ResponseMessage
                .error(406, "not_acceptable_media_type", msg)
                .result(e.getSupportedMediaTypes()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public Mono<ResponseMessage<Object>> handleException(MethodNotAllowedException e) {
        log.warn(e.getLocalizedMessage(), e);

        return LocaleUtils
            .resolveMessageReactive("error.method_not_allowed")
            .map(msg -> ResponseMessage
                .error(406, "method_not_allowed", msg)
                .result(e.getSupportedMethods()));
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<List<ValidationException.Detail>>> handleException(ServerWebInputException e) {
        Throwable exception = e;
        do {
            exception = exception.getCause();
            if (exception instanceof ValidationException) {
                return handleException(((ValidationException) exception));
            }

        } while (exception != null && exception != e);
        if (exception == null) {
            return Mono.just(
                ResponseMessage.error(400, CodeConstants.Error.illegal_argument, e.getMessage())
            );
        }
        return LocaleUtils
            .resolveThrowable(exception,
                              (err, msg) -> ResponseMessage.error(400, CodeConstants.Error.illegal_argument, msg));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<Object>> handleException(I18nSupportException e) {
        return e.getLocalizedMessageReactive()
                .map(msg -> ResponseMessage.error(400, e.getI18nCode(), msg));
    }

}
