package org.hsweb.web.controller;

import com.alibaba.fastjson.JSON;
import org.hsweb.web.core.exception.*;
import org.hsweb.web.core.message.ResponseMessage;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class ControllerExceptionTranslator {

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
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    ResponseMessage handleException(BusinessException exception) {
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

//    @ExceptionHandler(BusinessException.class)
//    ModelAndView handleExceptionView(BusinessException exception, HttpServletResponse response) {
//        response.setStatus(exception.getStatus());
//        ModelAndView modelAndView = new ModelAndView("error/" + exception.getStatus());
//        modelAndView.addAllObjects(ResponseMessage.error(exception.getMessage(), exception.getStatus()).toMap());
//        modelAndView.addObject("exception", exception);
//        return modelAndView;
//    }
//
//    @ExceptionHandler(Throwable.class)
//    ModelAndView handleExceptionView(Throwable exception, HttpServletResponse response) {
//        response.setStatus(500);
//        ModelAndView modelAndView = new ModelAndView("error/" + 500);
//        modelAndView.addAllObjects(ResponseMessage.error(exception.getMessage(), 500).toMap());
//        modelAndView.addObject("exception", exception);
//        return modelAndView;
//    }
}