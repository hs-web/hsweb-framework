package org.hswebframework.web.system.authorization.defaults.webflux;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Authorize(permission = "user",description = "用户管理")
public class WebFluxUserController {



}
