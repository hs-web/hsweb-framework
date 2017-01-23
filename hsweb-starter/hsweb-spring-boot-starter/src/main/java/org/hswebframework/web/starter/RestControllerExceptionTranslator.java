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

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.AuthorizeException;
import org.hswebframework.web.AuthorizeForbiddenException;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.validate.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class RestControllerExceptionTranslator {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseMessage handleException(ValidationException exception) {
        return ResponseMessage.error(exception.getMessage(), 400);
    }

    @ExceptionHandler(org.hsweb.ezorm.rdb.exception.ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseMessage handleException(org.hsweb.ezorm.rdb.exception.ValidationException exception) {
        return ResponseMessage.error(JSON.toJSONString(exception.getValidateResult()), 400);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    ResponseMessage handleException(BusinessException exception) {
        if (exception.getCause() != null) {
            logger.error("{}:{}", exception.getMessage(), exception.getStatus(), exception.getCause());
        }
        return ResponseMessage.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler(AuthorizeException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    ResponseMessage handleException(AuthorizeException exception) {
        return ResponseMessage.error(exception.getMessage(), exception.getStatus());
    }

    @ExceptionHandler(AuthorizeForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    ResponseMessage handleException(AuthorizeForbiddenException exception) {
        return ResponseMessage.error(exception.getMessage(), exception.getStatus());
    }


    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    ResponseMessage handleException(NotFoundException exception) {
        return ResponseMessage.error(exception.getMessage(), 404);
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