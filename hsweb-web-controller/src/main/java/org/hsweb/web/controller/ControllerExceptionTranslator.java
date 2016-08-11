package org.hsweb.web.controller;

import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.message.ResponseMessage;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice(annotations = Controller.class)
@Order(10)
public class ControllerExceptionTranslator {
    @ExceptionHandler(BusinessException.class)
    ModelAndView handleExceptionView(BusinessException exception, HttpServletResponse response) {
        response.setStatus(exception.getStatus());
        ModelAndView modelAndView = new ModelAndView("error/" + exception.getStatus());
        modelAndView.addAllObjects(ResponseMessage.error(exception.getMessage(), exception.getStatus()).toMap());
        modelAndView.addObject("exception", exception);
        return modelAndView;
    }

    @ExceptionHandler(Throwable.class)
    ModelAndView handleExceptionView(Throwable exception, HttpServletResponse response) {
        response.setStatus(500);
        ModelAndView modelAndView = new ModelAndView("error/" + 500);
        modelAndView.addAllObjects(ResponseMessage.error(exception.getMessage(), 500).toMap());
        modelAndView.addObject("exception", exception);
        return modelAndView;
    }
}