package org.hswebframework.web.authorization.basic.handler;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;

/**
 * @author zhouhao
 * @since 3.0.1
 */
public class TestController {

    public ResponseMessage<String> query() {
        return ResponseMessage.ok();
    }

    public ResponseMessage<String> update() {
        return ResponseMessage.ok();
    }

    public ResponseMessage<String> delete() {
        return ResponseMessage.ok();
    }

}
