package org.hsweb.web.exception;

import org.hsweb.web.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by zhouhao on 16-4-26.
 */
@Configuration
public class ExceptionHandlerConfiguration {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
                super.resolveException(request, response, handler, ex);
                if (ex instanceof BusinessException) {
                    try {
                        response.sendError(((BusinessException) ex).getStatus());
                    } catch (IOException e) {
                        logger.error("response.sendError", e);
                    }
                }
                return null;
            }

            @Override
            public Map<String, Object> getErrorAttributes(RequestAttributes requestAttributes,
                                                          boolean includeStackTrace) {
                Integer status = getAttribute(requestAttributes,
                        "javax.servlet.error.status_code");
                ResponseMessage responseMessage = new ResponseMessage(false, getError(requestAttributes), status == null ? "" : status.toString());
                return responseMessage.toMap();
            }

            public <T> T getAttribute(RequestAttributes requestAttributes, String name) {
                return (T) requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
            }
        };
    }
}
