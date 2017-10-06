/*
 * Copyright 2016 http://www.hswebframework.org
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
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.validate.SimpleValidateResults;
import org.hswebframework.web.validate.ValidateResults;
import org.hswebframework.web.validate.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class RestControllerExceptionTranslator {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(JSONException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseMessage handleException(JSONException exception) {
        logger.error("json error", exception);
        return ResponseMessage.error(400, exception.getMessage());
    }

    @ExceptionHandler(org.hswebframework.ezorm.rdb.exception.ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseMessage<Object> handleException(org.hswebframework.ezorm.rdb.exception.ValidationException exception) {
        return ResponseMessage.error(400, exception.getMessage()).result(exception.getValidateResult());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseMessage<List<ValidateResults.Result>> handleException(ValidationException exception) {
        return ResponseMessage.<List<ValidateResults.Result>>error(400, exception.getMessage())
                .result(exception.getResults());
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ResponseMessage handleException(BusinessException exception) {
        if (exception.getCause() != null) {
            logger.error("{}:{}", exception.getMessage(), exception.getStatus(), exception.getCause());
        }
        return ResponseMessage.error(exception.getStatus(), exception.getMessage());
    }

    @ExceptionHandler(UnAuthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    ResponseMessage handleException(UnAuthorizedException exception) {
        return ResponseMessage.error(401, exception.getMessage()).result(exception.getState());
    }

    @ExceptionHandler(AccessDenyException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    ResponseMessage handleException(AccessDenyException exception) {
        return ResponseMessage.error(403, exception.getMessage());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    ResponseMessage handleException(NotFoundException exception) {
        return ResponseMessage.error(404, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseMessage handleException(MethodArgumentNotValidException e) {
        SimpleValidateResults results = new SimpleValidateResults();
        e.getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .forEach(fieldError -> results.addResult(fieldError.getField(), fieldError.getDefaultMessage()));

        return ResponseMessage.error(400, results.getResults().size() == 0 ? e.getMessage() : results.getResults().get(0).getMessage()).result(results.getResults());
    }
//    @ExceptionHandler(Throwable.class)
//    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
//    @ResponseBody
//    @Order()
//    ResponseMessage handleException(Throwable exception) {
//        logger.error(exception.getMessage(), exception);
//        return ResponseMessage.error(exception.getMessage(), 500);
//    }

}