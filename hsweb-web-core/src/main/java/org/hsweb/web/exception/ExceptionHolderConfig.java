package org.hsweb.web.exception;

import org.hsweb.web.message.ResponseMessage;
import org.springframework.boot.autoconfigure.web.DefaultErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Map;

/**
 * Created by zhouhao on 16-4-26.
 */
@Configuration
public class ExceptionHolderConfig {
    @Bean
    public DefaultErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
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
