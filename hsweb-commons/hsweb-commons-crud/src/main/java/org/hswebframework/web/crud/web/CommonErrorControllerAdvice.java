package org.hswebframework.web.crud.web;

import io.r2dbc.spi.R2dbcDataIntegrityViolationException;
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
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.*;
import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestControllerAdvice
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@Slf4j
@Order
public class CommonErrorControllerAdvice {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseMessage<Object>> handleException(BusinessException e) {
        return LocaleUtils
                .resolveThrowable(e,
                                  (err, msg) -> ResponseMessage.error(err.getStatus(), err.getCode(), msg));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseMessage<Object>> handleException(UnsupportedOperationException e) {
        return LocaleUtils
                .resolveThrowable(e, (err, msg) -> (ResponseMessage.error(500, CodeConstants.Error.unsupported, msg)))
                .doOnEach(ReactiveLogger.onNext(r -> log.error(e.getMessage(), e)));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ResponseMessage<TokenState>> handleException(UnAuthorizedException e) {
        return LocaleUtils
                .resolveThrowable(e, (err, msg) -> (ResponseMessage.<TokenState>error(401, CodeConstants.Error.unauthorized, msg)
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
                .resolveThrowable(e, (err, msg) -> ResponseMessage
                        .<List<ValidationException.Detail>>error(400, CodeConstants.Error.illegal_argument, msg)
                        .result(e.getDetails()))
                ;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<List<ValidationException.Detail>>> handleException(ConstraintViolationException e) {
        return handleException(new ValidationException(e.getConstraintViolations()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<List<ValidationException.Detail>>> handleException(BindException e) {
        return handleException(new ValidationException(e.getMessage(), e
                .getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .map(err -> new ValidationException.Detail(err.getField(), err.getDefaultMessage(), null))
                .collect(Collectors.toList())));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<List<ValidationException.Detail>>> handleException(WebExchangeBindException e) {
        return handleException(new ValidationException(e.getMessage(), e
                .getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .map(err -> new ValidationException.Detail(err.getField(), err.getDefaultMessage(), null))
                .collect(Collectors.toList())));
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<List<ValidationException.Detail>>> handleException(MethodArgumentNotValidException e) {
        return handleException(new ValidationException(e.getMessage(), e
                .getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .map(err -> new ValidationException.Detail(err.getField(), err.getDefaultMessage(), null))
                .collect(Collectors.toList())));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<?>> handleException(javax.validation.ValidationException e) {
        return Mono.just(ResponseMessage.error(400, CodeConstants.Error.illegal_argument, e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public Mono<ResponseMessage<Object>> handleException(TimeoutException e) {
        return LocaleUtils
                .resolveThrowable(e, (err, msg) -> ResponseMessage.error(504, CodeConstants.Error.timeout,msg))
                .doOnEach(ReactiveLogger.onNext(r -> log.error(e.getMessage(), e)));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order
    public Mono<ResponseMessage<Object>> handleException(RuntimeException e) {
        return LocaleUtils
                .resolveThrowable(e, (err, msg) -> ResponseMessage.error(msg))
                .doOnEach(ReactiveLogger.onNext(r -> log.error(e.getMessage(), e)));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseMessage<Object>> handleException(NullPointerException e) {

        return Mono.just(ResponseMessage.error(e.getMessage()))
                   .doOnEach(ReactiveLogger.onNext(r -> log.error(e.getMessage(), e)));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<Object>> handleException(IllegalArgumentException e) {

        return LocaleUtils
                .resolveThrowable(e, (err, msg) -> ResponseMessage.error(400, CodeConstants.Error.illegal_argument, msg))
                .doOnEach(ReactiveLogger.onNext(r -> log.error(e.getMessage(), e)))
                ;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<Object>> handleException(AuthenticationException e) {
        return LocaleUtils
                .resolveThrowable(e, (err, msg) -> ResponseMessage.error(400, err.getCode(), msg))
                .doOnEach(ReactiveLogger.onNext(r -> log.error(e.getLocalizedMessage(), e)))
                ;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Mono<ResponseMessage<Object>> handleException(UnsupportedMediaTypeStatusException e) {
        return LocaleUtils
                .resolveMessageReactive("error.unsupported_media_type")
                .map(msg -> ResponseMessage
                        .error(415, "unsupported_media_type", msg)
                        .result(e.getSupportedMediaTypes()))
                .doOnEach(ReactiveLogger.onNext(r -> log.error(e.getLocalizedMessage(), e)));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public Mono<ResponseMessage<Object>> handleException(NotAcceptableStatusException e) {

        return LocaleUtils
                .resolveMessageReactive("error.not_acceptable_media_type")
                .map(msg -> ResponseMessage
                        .error(406, "not_acceptable_media_type", msg)
                        .result(e.getSupportedMediaTypes()))
                .doOnEach(ReactiveLogger.onNext(r -> log.error(e.getMessage(), e)));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public Mono<ResponseMessage<Object>> handleException(MethodNotAllowedException e) {
        return LocaleUtils
                .resolveMessageReactive("error.method_not_allowed")
                .map(msg -> ResponseMessage
                        .error(406, "method_not_allowed", msg)
                        .result(e.getSupportedMethods()))
                .doOnEach(ReactiveLogger.onNext(r -> log.error(e.getMessage(), e)));
    }

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

        return LocaleUtils
                .resolveThrowable(exception,
                                  (err, msg) -> ResponseMessage.error(400, CodeConstants.Error.illegal_argument, msg));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseMessage<Object>> handleException(I18nSupportException e) {
        return LocaleUtils
                .resolveThrowable(e,
                                  (err, msg) -> ResponseMessage.error(400, err.getCode(), msg));
    }

}
