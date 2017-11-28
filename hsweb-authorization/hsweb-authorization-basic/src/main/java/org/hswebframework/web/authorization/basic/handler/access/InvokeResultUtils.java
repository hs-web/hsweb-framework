package org.hswebframework.web.authorization.basic.handler.access;

import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.http.ResponseEntity;

public class InvokeResultUtils {
    public static Object convertRealResult(Object result) {
        if (result instanceof ResponseMessage) {
            return ((ResponseMessage) result).getResult();
        }
        if (result instanceof ResponseEntity) {
            return ((ResponseEntity) result).getBody();
        }
        return result;
    }
}
