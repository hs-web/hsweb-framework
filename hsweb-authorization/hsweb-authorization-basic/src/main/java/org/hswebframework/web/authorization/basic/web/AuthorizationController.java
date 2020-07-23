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

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.SneakyThrows;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.events.AuthorizationBeforeEvent;
import org.hswebframework.web.authorization.events.AuthorizationDecodeEvent;
import org.hswebframework.web.authorization.events.AuthorizationFailedEvent;
import org.hswebframework.web.authorization.events.AuthorizationSuccessEvent;
import org.hswebframework.web.authorization.exception.AuthenticationException;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.simple.CompositeReactiveAuthenticationManager;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.logging.AccessLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.Function;

/**
 * @author zhouhao
 */
@RestController
@RequestMapping("${hsweb.web.mappings.authorize:authorize}")
public class AuthorizationController {


    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @GetMapping("/me")
    @Authorize
    @ApiOperation("当前登录用户权限信息")
    public Mono<Authentication> me() {
        return Authentication.currentReactive()
                .switchIfEmpty(Mono.error(UnAuthorizedException::new));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("用户名密码登录,json方式")
    @Authorize(ignore = true)
    @AccessLogger(ignore = true)
    public Mono<Map<String, Object>> authorizeByJson(@ApiParam(example = "{\"username\":\"admin\",\"password\":\"admin\"}")
                                                     @RequestBody Mono<Map<String, Object>> parameter) {
        return doLogin(parameter);
    }

    /**
     * <img src="https://raw.githubusercontent.com/hs-web/hsweb-framework/4.0.x/hsweb-authorization/hsweb-authorization-basic/img/autz-flow.png">
     */
    @SneakyThrows
    private Mono<Map<String, Object>> doLogin(Mono<Map<String, Object>> parameter) {

        return parameter.flatMap(parameters -> {
            String username_ = (String) parameters.get("username");
            String password_ = (String) parameters.get("password");

            Assert.hasLength(username_, "用户名不能为空");
            Assert.hasLength(password_, "密码不能为空");

            AuthorizationFailedEvent.Reason reason = AuthorizationFailedEvent.Reason.OTHER;
            Function<String, Object> parameterGetter = parameters::get;
            return Mono.defer(() -> {
                AuthorizationDecodeEvent decodeEvent = new AuthorizationDecodeEvent(username_, password_, parameterGetter);
                return decodeEvent
                        .publish(eventPublisher)
                        .then(Mono.defer(() -> {
                            String username = decodeEvent.getUsername();
                            String password = decodeEvent.getPassword();
                            AuthorizationBeforeEvent beforeEvent = new AuthorizationBeforeEvent(username, password, parameterGetter);
                            return beforeEvent
                                    .publish(eventPublisher)
                                    .then(authenticationManager
                                            .authenticate(Mono.just(new PlainTextUsernamePasswordAuthenticationRequest(username, password)))
                                            .switchIfEmpty(Mono.error(() -> new AuthenticationException(AuthenticationException.ILLEGAL_PASSWORD,"密码错误")))
                                            .flatMap(auth -> {
                                                //触发授权成功事件
                                                AuthorizationSuccessEvent event = new AuthorizationSuccessEvent(auth, parameterGetter);
                                                event.getResult().put("userId", auth.getUser().getId());
                                                return event
                                                        .publish(eventPublisher)
                                                        .then(Mono.fromCallable(event::getResult));
                                            }));
                        }));
            }).onErrorResume(err -> {
                AuthorizationFailedEvent failedEvent = new AuthorizationFailedEvent(username_, password_, parameterGetter, reason);
                failedEvent.setException(err);
                return failedEvent
                        .publish(eventPublisher)
                        .then(Mono.error(failedEvent.getException()));
            });
        });

    }

}
