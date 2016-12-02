package org.hsweb.web.controller;

import org.hsweb.commons.ClassUtils;
import org.hsweb.ezorm.rdb.exception.ValidationException;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.exception.ExceptionHandler;
import org.hsweb.web.core.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouhao on 16-4-26.
 */
@Configuration
public class ExceptionHandlerConfiguration {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private List<ExceptionHandler> exceptionHandlers;


    @Bean
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
                super.resolveException(request, response, handler, ex);
                ResponseMessage responseMessage = null;
                //获取自定义的异常处理器
                for (ExceptionHandler exceptionHandler : exceptionHandlers) {
                    if (exceptionHandler.support(ex.getClass())) {
                        responseMessage = exceptionHandler.handle(ex);
                        break;
                    }
                }
                if (responseMessage == null) {
                    responseMessage = ResponseMessage.error(ex.getMessage());
                }
                request.setAttribute("error.attributes", responseMessage.toMap());
//                try {
                request.setAttribute("javax.servlet.error.status_code", responseMessage.getCode());
//                    response.sendError(responseMessage.getCode());
//                } catch (IOException e) {
//                    logger.error("", e);
//                }
                return null;
            }

            @Override
            public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes,
                                                          boolean includeStackTrace) {
                Map<String, Object> attrs = ((Map) requestAttributes.getAttribute("error.attributes", RequestAttributes.SCOPE_REQUEST));
                //错误属性为空，说明可能不是由controller抛出的一次信息
                if (attrs == null) {
                    Integer status = (Integer) this.getAttribute(requestAttributes, "javax.servlet.error.status_code");
                    Object message = this.getAttribute(requestAttributes, "javax.servlet.error.message");
                    Throwable throwable = getError(requestAttributes);
                    if (throwable != null) logger.error("", throwable);
                    if (message == null)
                        message = "None";
                    return ResponseMessage.error(String.valueOf(message), status).toMap();
                }
                return attrs;
            }

            public Object getAttribute(RequestAttributes requestAttributes, String name) {
                return requestAttributes.getAttribute(name, 0);
            }
        };
    }

    @Bean
    @Order(1000)
    public ExceptionHandler defaultExceptionHandler() {
        ExceptionHandler handler = new ExceptionHandler() {
            @Override
            public <T extends Throwable> boolean support(Class<T> e) {
                return true;
            }

            @Override
            public ResponseMessage handle(Throwable e) {
                logger.error("", e);
                return ResponseMessage.error(e.getMessage());
            }
        };
        return handler;
    }

    @Bean
    @Order(600)
    public ExceptionHandler validationExceptionHandler() {
        ExceptionHandler handler = new ExceptionHandler() {
            @Override
            public <T extends Throwable> boolean support(Class<T> e) {
                return ClassUtils.instanceOf(e, ValidationException.class);
            }

            @Override
            public ResponseMessage handle(Throwable e) {
                return ResponseMessage.error(String.valueOf(((ValidationException) e).getValidateResult()), 400);
            }
        };
        return handler;
    }

    @Bean
    @Order(900)
    public ExceptionHandler businessExceptionHandler() {
        ExceptionHandler handler = new ExceptionHandler() {
            @Override
            public <T extends Throwable> boolean support(Class<T> e) {
                return ClassUtils.instanceOf(e, BusinessException.class);
            }

            @Override
            public ResponseMessage handle(Throwable e) {
                return ResponseMessage.error(e.getMessage(), ((BusinessException) e).getStatus());
            }
        };
        return handler;
    }

}
