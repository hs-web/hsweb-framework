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
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.NotAcceptableStatusException;
import org.springframework.web.server.ServerWebInputException;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;
import reactor.core.publisher.Mono;

import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@Order
public class CommonWebMvcErrorControllerAdvice {

    private String resolveMessage(Throwable e) {
        if (e instanceof I18nSupportException) {
            return LocaleUtils.resolveMessage(((I18nSupportException) e).getI18nCode());
        }
        return e.getMessage() == null ? null : LocaleUtils.resolveMessage(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseMessage<Object> handleException(BusinessException err) {
        String msg = resolveMessage(err);
        return ResponseMessage.error(err.getStatus(), err.getCode(), msg);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseMessage<Object> handleException(UnsupportedOperationException e) {
        log.warn(e.getLocalizedMessage(), e);
        String msg = resolveMessage(e);
        return ResponseMessage.error(500, CodeConstants.Error.unsupported, msg);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseMessage<TokenState> handleException(UnAuthorizedException e) {
        return ResponseMessage
                .<TokenState>error(401, CodeConstants.Error.unauthorized, resolveMessage(e))
                .result(e.getState());

    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseMessage<Object> handleException(AccessDenyException e) {
        return ResponseMessage.error(403, e.getCode(), resolveMessage(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseMessage<Object> handleException(NotFoundException e) {
        return ResponseMessage.error(404, CodeConstants.Error.not_found, resolveMessage(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<List<ValidationException.Detail>> handleException(ValidationException e) {

        return ResponseMessage
                .<List<ValidationException.Detail>>error(400, CodeConstants.Error.illegal_argument, resolveMessage(e))
                .result(e.getDetails())
                ;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<List<ValidationException.Detail>> handleException(ConstraintViolationException e) {
        return handleException(new ValidationException(e.getConstraintViolations()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<List<ValidationException.Detail>> handleException(BindException e) {
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
    public ResponseMessage<List<ValidationException.Detail>> handleException(WebExchangeBindException e) {
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
    public ResponseMessage<List<ValidationException.Detail>> handleException(MethodArgumentNotValidException e) {
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
    public ResponseMessage<?> handleException(jakarta.validation.ValidationException e) {
        return ResponseMessage.error(400, CodeConstants.Error.illegal_argument, e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    public ResponseMessage<Object> handleException(TimeoutException e) {
        return ResponseMessage.error(504, CodeConstants.Error.timeout, resolveMessage(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @Order
    public ResponseMessage<Object> handleException(RuntimeException e) {
        log.warn(e.getLocalizedMessage(), e);
        return ResponseMessage.error(resolveMessage(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseMessage<Object> handleException(NullPointerException e) {
        log.warn(e.getLocalizedMessage(), e);
        return ResponseMessage.error(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Object> handleException(IllegalArgumentException e) {
        log.warn(e.getLocalizedMessage(), e);

        return ResponseMessage.error(400, CodeConstants.Error.illegal_argument, resolveMessage(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Object> handleException(AuthenticationException e) {
        log.warn(e.getLocalizedMessage(), e);

        return ResponseMessage.error(400, e.getCode(), resolveMessage(e));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseMessage<Object> handleException(UnsupportedMediaTypeStatusException e) {
        log.warn(e.getLocalizedMessage(), e);

        return ResponseMessage
                .error(415, "unsupported_media_type", LocaleUtils.resolveMessage("error.unsupported_media_type"))
                .result(e.getSupportedMediaTypes());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseMessage<Object> handleException(NotAcceptableStatusException e) {
        log.warn(e.getLocalizedMessage(), e);

        return ResponseMessage
                .error(406, "not_acceptable_media_type", LocaleUtils
                        .resolveMessage("error.not_acceptable_media_type"))
                .result(e.getSupportedMediaTypes());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseMessage<Object> handleException(MethodNotAllowedException e) {
        log.warn(e.getLocalizedMessage(), e);

        return ResponseMessage
                .error(406, "method_not_allowed", LocaleUtils.resolveMessage("error.method_not_allowed"))
                .result(e.getSupportedMethods());
    }


    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<List<ValidationException.Detail>> handleException(ServerWebInputException e) {
        Throwable exception = e;
        do {
            exception = exception.getCause();
            if (exception instanceof ValidationException) {
                return handleException(((ValidationException) exception));
            }

        } while (exception != null && exception != e);
        if (exception == null) {
            return  ResponseMessage.error(400, CodeConstants.Error.illegal_argument, e.getMessage());
        }
        return ResponseMessage.error(400, CodeConstants.Error.illegal_argument, resolveMessage(exception));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseMessage<Object> handleException(I18nSupportException e) {
        return ResponseMessage.error(400, e.getI18nCode(), resolveMessage(e));
    }

}
