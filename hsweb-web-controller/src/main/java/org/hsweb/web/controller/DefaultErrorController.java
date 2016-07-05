package org.hsweb.web.controller;

import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Controller
@RequestMapping("/error")
@AccessLogger("错误请求")
public class DefaultErrorController implements ErrorController {

    @Autowired
    private ErrorAttributes errorAttributes;

    @RequestMapping
    @AccessLogger("html")
    public ModelAndView errorHtml(HttpServletRequest request, HttpServletResponse response) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        Map<String, Object> model = errorAttributes.getErrorAttributes(requestAttributes, true);
        int code = ((int) model.get("code"));
        response.setStatus(code);
        return new ModelAndView("error/" + response.getStatus(), model);
    }

    @RequestMapping(produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_JSON_UTF8_VALUE})
    @ResponseBody
    @AccessLogger("json")
    public Object error(HttpServletRequest request, HttpServletResponse response) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(request);
        Map<String, Object> model = errorAttributes.getErrorAttributes(requestAttributes, true);
        int code = ((int) model.get("code"));
        response.setStatus(code);
        return model;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }
}