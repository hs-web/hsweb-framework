package org.hsweb.web.controller;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.core.exception.*;
import org.hsweb.web.core.message.ResponseMessage;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice(annotations = {RestController.class, ResponseBody.class})
@Order(1)
public class RestControllerExceptionTranslator {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseMessage handleException(ValidationException exception) {
        return ResponseMessage.error(exception.getMessage(), 400);
    }

    @ExceptionHandler(org.hsweb.ezorm.exception.ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    ResponseMessage handleException(org.hsweb.ezorm.exception.ValidationException exception) {
        return ResponseMessage.error(JSON.toJSONString(exception.getValidateResult()), 400);
    }


    @ExceptionHandler(BusinessException.class)
    @ResponseBody
    ResponseMessage handleException(BusinessException exception, HttpServletResponse response) {
        response.setStatus(exception.getStatus());
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

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    ResponseMessage handleException(Throwable exception) {
        return ResponseMessage.error(exception.getMessage(), 500);
    }

}