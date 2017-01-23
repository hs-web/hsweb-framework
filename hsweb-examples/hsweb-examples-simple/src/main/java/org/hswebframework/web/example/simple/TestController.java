package org.hswebframework.web.example.simple;

import org.apache.shiro.authz.annotation.RequiresUser;
import org.hswebframework.web.authorization.Authorization;
import org.hswebframework.web.authorization.annotation.AuthInfo;
import org.hswebframework.web.authorization.annotation.RequiresExpression;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RestController
public class TestController {
    @GetMapping("/test")
    @RequiresUser
    @RequiresExpression("#authorization!=null&&#authorization.user.username=='admin'")
    public ResponseMessage testShiro(@AuthInfo Authorization authorization) {
        return ResponseMessage.ok(authorization);
    }

}
