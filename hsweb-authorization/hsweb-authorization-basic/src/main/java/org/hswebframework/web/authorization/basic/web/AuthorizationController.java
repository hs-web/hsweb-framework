/*
 * Copyright 2019 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.authorization.basic.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.SneakyThrows;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.events.*;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.logging.AccessLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zhouhao
 */
@RestController
@RequestMapping("${hsweb.web.mappings.authorize:authorize}")
@AccessLogger("授权")
@Api(tags = "权限-用户授权", value = "授权")
public class AuthorizationController {

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @GetMapping("/me")
    @Authorize
    @ApiOperation("当前登录用户权限信息")
    public Mono<Authentication> me(@ApiParam(hidden = true) Mono<Authentication> authentication) {
        return authentication;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户名密码登录,json方式")
    public Mono<Map<String, Object>> authorizeByJson(@ApiParam(example = "{\"username\":\"admin\",\"password\":\"admin\"}")
                                                     @RequestBody Map<String, Object> parameter) {
        return doLogin(Mono.just(parameter));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiOperation("用户名密码登录,参数方式")
    public Mono<Map<String, Object>> authorizeByUrlEncoded(@ApiParam(hidden = true) @RequestParam Mono<Map<String, Object>> parameter) {

        return doLogin(parameter);
    }

    /**
     * <img src="https://raw.githubusercontent.com/hs-web/hsweb-framework/3.0.x/hsweb-authorization/hsweb-authorization-basic/img/autz-flow.png">
     */
    @SneakyThrows
    protected Mono<Map<String, Object>> doLogin(Mono<Map<String, Object>> parameter) {

        return parameter.flatMap(parameters -> {
            String username = (String) parameters.get("username");
            String password = (String) parameters.get("password");

            Assert.hasLength(username, "用户名不能为空");
            Assert.hasLength(password, "密码不能为空");

            AuthorizationFailedEvent.Reason reason = AuthorizationFailedEvent.Reason.OTHER;
            Function<String, Object> parameterGetter = parameters::get;
            try {
                AuthorizationDecodeEvent decodeEvent = new AuthorizationDecodeEvent(username, password, parameterGetter);
                eventPublisher.publishEvent(decodeEvent);
                username = decodeEvent.getUsername();
                password = decodeEvent.getPassword();
                AuthorizationBeforeEvent beforeEvent = new AuthorizationBeforeEvent(username, password, parameterGetter);
                eventPublisher.publishEvent(beforeEvent);
                // 验证通过
                return authenticationManager
                        .authenticate(Mono.just(new PlainTextUsernamePasswordAuthenticationRequest(username, password)))
                        .map(auth -> {
                            //触发授权成功事件
                            AuthorizationSuccessEvent event = new AuthorizationSuccessEvent(auth, parameterGetter);
                            event.getResult().put("userId", auth.getUser().getId());
                            eventPublisher.publishEvent(event);
                            return event.getResult();
                        });
            } catch (Exception e) {
                AuthorizationFailedEvent failedEvent = new AuthorizationFailedEvent(username, password, parameterGetter, reason);
                failedEvent.setException(e);
                eventPublisher.publishEvent(failedEvent);
                return Mono.error(failedEvent.getException());
            }
        });

    }

}
