package org.hswebframework.web.examples.cloud.service;

import org.hswebframework.web.authorization.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by zhouhao on 2017/10/10.
 */
@RestController
@RequestMapping("/user-info")
public class UserInfoController {

    @GetMapping
    public Authentication authentication(){
        return Authentication.current().orElse(null);
    }
}
