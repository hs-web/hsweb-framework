package org.hswebframework.web.authorization.basic.handler.access;

import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.http.ResponseEntity;

public class InvokeResultUtils {
    public static Object convertRealResult(Object result) {
        if (result instanceof ResponseEntity) {
            result = ((ResponseEntity) result).getBody();
        }
        if (result instanceof ResponseMessage) {
            result = ((ResponseMessage) result).getResult();
        }
        if (result instanceof PagerResult) {
            result = ((PagerResult) result).getData();
        }
        return result;
    }
}
