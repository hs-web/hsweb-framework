package org.hswebframework.web.authorization.basic.web;

import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.ReactiveAuthenticationManager;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.context.ContextKey;
import org.hswebframework.web.context.ContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping
@Authorize
@Resource(id = "user-token", name = "用户令牌信息管理")
public class ReactiveUserTokenController {
    private UserTokenManager userTokenManager;

    private ReactiveAuthenticationManager authenticationManager;

    @Autowired
    @Lazy
    public void setUserTokenManager(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    @Autowired
    @Lazy
    public void setAuthenticationManager(ReactiveAuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/user-token/reset")
    @Authorize(merge = false)
    @ApiOperation("重置当前用户的令牌")
    public Mono<Boolean> resetToken() {
        return ContextUtils.reactiveContext()
                .map(context -> context.get(ContextKey.of(ParsedToken.class)).orElseThrow(UnAuthorizedException::new))
                .flatMap(token -> userTokenManager.signOutByToken(token.getToken()))
                .thenReturn(true);
    }

    @PutMapping("/user-token/check")
    @ApiOperation("检查所有已过期的token并移除")
    @SaveAction
    public Mono<Boolean> checkExpiredToken() {
        return userTokenManager
                .checkExpiredToken()
                .thenReturn(true);
    }

    @GetMapping("/user-token/token/{token}")
    @ApiOperation("根据token获取令牌信息")
    @QueryAction
    public Mono<UserToken> getByToken(@PathVariable String token) {
        return userTokenManager.getByToken(token);
    }

    @GetMapping("/user-token/user/{userId}")
    @ApiOperation("根据用户ID获取全部令牌信息")
    @QueryAction
    public Flux<UserToken> getByUserId(@PathVariable String userId) {
        return userTokenManager.getByUserId(userId);
    }

    @GetMapping("/user-token/user/{userId}/logged")
    @ApiOperation("根据用户ID判断用户是否已经登录")
    @QueryAction
    public Mono<Boolean> userIsLoggedIn(@PathVariable String userId) {
        return userTokenManager.userIsLoggedIn(userId);
    }

    @GetMapping("/user-token/token/{token}/logged")
    @ApiOperation("根据令牌判断用户是否已经登录")
    @QueryAction
    public Mono<Boolean> tokenIsLoggedIn(@PathVariable String token) {
        return userTokenManager.tokenIsLoggedIn(token);
    }

    @GetMapping("/user-token/user/total")
    @ApiOperation("获取当前已经登录的用户数量")
    @Authorize(merge = false)
    public Mono<Integer> totalUser() {
        return userTokenManager.totalUser();
    }

    @GetMapping("/user-token/token/total")
    @ApiOperation("获取当前已经登录的令牌数量")
    @Authorize(merge = false)
    public Mono<Integer> totalToken() {
        return userTokenManager.totalToken();
    }

    @GetMapping("/user-token")
    @ApiOperation("获取全部用户令牌信息")
    @QueryAction
    public Flux<UserToken> allLoggedUser() {
        return userTokenManager.allLoggedUser();
    }

    @DeleteMapping("/user-token/user/{userId}")
    @ApiOperation("根据用户id将用户踢下线")
    @SaveAction
    public Mono<Void> signOutByUserId(@PathVariable String userId) {
        return userTokenManager.signOutByUserId(userId);
    }

    @DeleteMapping("/user-token/token/{token}")
    @ApiOperation("根据令牌将用户踢下线")
    @SaveAction
    public Mono<Void> signOutByToken(@PathVariable String token) {
        return userTokenManager.signOutByToken(token);

    }

    @SaveAction
    @PutMapping("/user-token/user/{userId}/{state}")
    @ApiOperation("根据用户id更新用户令牌状态")
    public Mono<Void> changeUserState(@PathVariable String userId, @PathVariable TokenState state) {

        return userTokenManager.changeUserState(userId, state);
    }

    @PutMapping("/user-token/token/{token}/{state}")
    @ApiOperation("根据令牌更新用户令牌状态")
    @SaveAction
    public Mono<Void> changeTokenState(@PathVariable String token, @PathVariable TokenState state) {
        return userTokenManager.changeTokenState(token, state);
    }

    @PostMapping("/user-token/{token}/{type}/{userId}/{maxInactiveInterval}")
    @ApiOperation("将用户设置为登录")
    @SaveAction
    public Mono<UserToken> signIn(@PathVariable String token, @PathVariable String type, @PathVariable String userId, @PathVariable long maxInactiveInterval) {
        return userTokenManager.signIn(token, type, userId, maxInactiveInterval);
    }

    @GetMapping("/user-token/{token}/touch")
    @ApiOperation("更新token有效期")
    @SaveAction
    public Mono<Void> touch(@PathVariable String token) {
        return userTokenManager.touch(token);
    }

    @GetMapping("/user-auth/{userId}")
    @ApiOperation("根据用户id获取用户的权限信息")
    @SaveAction
    public Mono<Authentication> userAuthInfo(@PathVariable String userId) {
        return authenticationManager.getByUserId(userId);
    }

}
