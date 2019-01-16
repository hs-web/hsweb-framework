/*
 * Copyright 2019 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.starter;

import com.alibaba.fastjson.JSONException;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.exception.NeedTwoFactorException;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.validate.SimpleValidateResults;
import org.hswebframework.web.validate.ValidateResults;
import org.hswebframework.web.validate.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class RestControllerExceptionTranslator {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(JSONException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseMessage handleException(JSONException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseMessage.error(400, "解析JSON失败");
    }

    @ExceptionHandler(org.hswebframework.ezorm.rdb.exception.ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseMessage<Object> handleException(org.hswebframework.ezorm.rdb.exception.ValidationException exception) {
        return ResponseMessage.error(400, exception.getMessage())
                .result(exception.getValidateResult());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseMessage<List<ValidateResults.Result>> handleException(ValidationException exception) {
        return ResponseMessage.<List<ValidateResults.Result>>error(400, exception.getMessage())
                .result(exception.getResults());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ResponseMessage handleException(BusinessException exception) {
        if (exception.getCause() != null) {
            logger.error("{}:{}", exception.getMessage(), exception.getStatus(), exception.getCause());
        }
        return ResponseMessage.error(exception.getStatus(), exception.getMessage()).result(exception.getCode());
    }

    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    ResponseMessage handleException(UnAuthorizedException exception) {
        return ResponseMessage.error(401, exception.getMessage()).result(exception.getState());
    }

    @ExceptionHandler(AccessDenyException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ResponseMessage handleException(AccessDenyException exception) {
        return ResponseMessage.error(403, exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseMessage handleException(NotFoundException exception) {
        return ResponseMessage.error(404, exception.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseMessage handleConstraintViolationException(ConstraintViolationException e) {
        SimpleValidateResults results = new SimpleValidateResults();
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            results.addResult(violation.getPropertyPath().toString(), violation.getMessage());
        }
        List<ValidateResults.Result> errorResults = results.getResults();
        return ResponseMessage
                .error(400, errorResults.isEmpty() ? "" : errorResults.get(0).getMessage())
                .result(errorResults);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseMessage handleException(BindException e) {
        SimpleValidateResults results = new SimpleValidateResults();
        e.getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .forEach(fieldError -> results.addResult(fieldError.getField(), fieldError.getDefaultMessage()));

        return ResponseMessage.error(400, results.getResults().isEmpty() ? e.getMessage() : results.getResults().get(0).getMessage()).result(results.getResults());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseMessage handleException(MethodArgumentNotValidException e) {
        SimpleValidateResults results = new SimpleValidateResults();
        e.getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .forEach(fieldError -> results.addResult(fieldError.getField(), fieldError.getDefaultMessage()));

        return ResponseMessage.error(400, results.getResults().isEmpty() ? e.getMessage() : results.getResults().get(0).getMessage()).result(results.getResults());
    }

    @ExceptionHandler(TimeoutException.class)
    @ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
    ResponseMessage handleException(TimeoutException exception) {
        String msg = Optional.ofNullable(exception.getMessage())
                .orElse("访问服务超时");
        logger.warn(exception.getMessage(), exception);
        return ResponseMessage.error(504, msg);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ResponseMessage handleException(RuntimeException exception) {
        String msg = Optional.ofNullable(exception.getMessage())
                .orElse("服务器内部错误");
        logger.error(exception.getMessage(), exception);
        return ResponseMessage.error(500, msg);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseMessage handleException(DuplicateKeyException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseMessage.error(400, "重复的请求");
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ResponseMessage handleException(DataAccessException e) {
        logger.error(e.getMessage(), e);
        return ResponseMessage.error(500, "服务异常");
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ResponseMessage handleException(NullPointerException exception) {
        String msg = Optional.ofNullable(exception.getMessage())
                .orElse("服务器内部错误");
        logger.error(exception.getMessage(), exception);
        return ResponseMessage.error(500, msg);
    }

    @ExceptionHandler(SQLException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ResponseMessage handleException(SQLException exception) {
        logger.error(exception.getMessage(), exception);
        return ResponseMessage.error(500, "服务器内部错误");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseMessage handleException(IllegalArgumentException exception) {
        String msg = exception.getMessage();
        if (null == msg) {
            logger.error(msg = "参数错误", exception);
        }
        return ResponseMessage.error(400, msg);
    }

    @ExceptionHandler(NeedTwoFactorException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    ResponseMessage handleException(NeedTwoFactorException e) {
        return ResponseMessage
                .error(403, e.getMessage())
                .code("need_tow_factor")
                .result(e.getProvider());
    }

    /**
     * 请求方式不支持异常
     * 比如：POST方式的API, GET方式请求
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    ResponseMessage handleException(HttpRequestMethodNotSupportedException exception) {
        return ResponseMessage
                .error(HttpStatus.METHOD_NOT_ALLOWED.value(), "不支持的请求方式")
                .result(exception.getSupportedHttpMethods());
    }

    /**
     * 404异常，Spring MVC DispatcherServlet 当没找到 Handler处理请求时，
     * 如果配置了 throwExceptionIfNoHandlerFound 为 true时，会抛出此异常
     * <p>
     * 在配置文件中使用：
     * spring:
     * mvc:
     * throw-exception-if-no-handler-found: true
     *
     * @see org.springframework.web.servlet.DispatcherServlet#noHandlerFound(HttpServletRequest, HttpServletResponse)
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    ResponseMessage handleException(NoHandlerFoundException exception) {
        Map<String, Object> result = new HashMap<>();
        result.put("url", exception.getRequestURL());
        result.put("method", exception.getHttpMethod());
        return ResponseMessage.error(HttpStatus.NOT_FOUND.value(), "请求地址不存在.");
    }

    /**
     * ContentType不支持异常
     * 比如：@RequestBody注解，需要Content-Type: application/json, 但是请求未指定使用的默认的 Content-Type: application/x-www-form-urlencoded
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    ResponseMessage handleException(HttpMediaTypeNotSupportedException exception) {
        return ResponseMessage.error(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                "不支持的请求类型:" + exception.getContentType().toString())
                .result(exception.getSupportedMediaTypes()
                        .stream()
                        .map(MediaType::toString)
                        .collect(Collectors.toList()));
    }

    /**
     * 请求方法的的参数缺失
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    ResponseMessage handleException(MissingServletRequestParameterException exception) {
        return ResponseMessage
                .error(HttpStatus.BAD_REQUEST.value(), "参数[" + exception.getParameterName() + "]不能为空");
    }

}