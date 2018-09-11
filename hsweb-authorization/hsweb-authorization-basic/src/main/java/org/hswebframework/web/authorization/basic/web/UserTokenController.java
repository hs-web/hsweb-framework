package org.hswebframework.web.authorization.basic.web;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenHolder;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

@RestController
@RequestMapping
@Authorize(permission = "user-token", description = "用户令牌信息管理")
@Api(tags = "权限-用户令牌管理", value = "权限-用户令牌管理")
public class UserTokenController {
    private UserTokenManager userTokenManager;

    private AuthenticationManager authenticationManager;

    @Autowired
    @Lazy
    public void setUserTokenManager(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    @Autowired
    @Lazy
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/user-token/reset")
    @Authorize(merge = false)
    @ApiOperation("重置当前用户的令牌")
    public ResponseMessage<Boolean> resetToken() {
        UserToken token = UserTokenHolder.currentToken();
        if (token != null) {
            userTokenManager.signOutByToken(token.getToken());
        }
        return ok(true);
    }

    @PutMapping("/user-token/check")
    @ApiOperation("检查所有已过期的token并移除")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Boolean> checkExpiredToken() {
        userTokenManager.checkExpiredToken();
        return ok(true);
    }

    @GetMapping("/user-token/token/{token}")
    @ApiOperation("根据token获取令牌信息")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<UserToken> getByToken(@PathVariable String token) {
        return ok(userTokenManager.getByToken(token));
    }

    @GetMapping("/user-token/user/{userId}")
    @ApiOperation("根据用户ID获取全部令牌信息")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<List<UserToken>> getByUserId(@PathVariable String userId) {
        return ok(userTokenManager.getByUserId(userId));
    }

    @GetMapping("/user-token/user/{userId}/logged")
    @ApiOperation("根据用户ID判断用户是否已经登录")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<Boolean> userIsLoggedIn(@PathVariable String userId) {
        return ok(userTokenManager.userIsLoggedIn(userId));
    }

    @GetMapping("/user-token/token/{token}/logged")
    @ApiOperation("根据令牌判断用户是否已经登录")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<Boolean> tokenIsLoggedIn(@PathVariable String token) {
        return ok(userTokenManager.tokenIsLoggedIn(token));
    }

    @GetMapping("/user-token/user/total")
    @ApiOperation("获取当前已经登录的用户数量")
    @Authorize
    public ResponseMessage<Long> totalUser() {
        return ok(userTokenManager.totalUser());
    }

    @GetMapping("/user-token/token/total")
    @ApiOperation("获取当前已经登录的令牌数量")
    @Authorize
    public ResponseMessage<Long> totalToken() {
        return ok(userTokenManager.totalToken());
    }

    @GetMapping("/user-token")
    @ApiOperation("获取全部用户令牌信息")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<List<UserToken>> allLoggedUser() {
        return ok(userTokenManager.allLoggedUser());
    }

    @DeleteMapping("/user-token/user/{userId}")
    @ApiOperation("根据用户id将用户踢下线")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Void> signOutByUserId(@PathVariable String userId) {
        userTokenManager.signOutByUserId(userId);
        return ok();
    }

    @DeleteMapping("/user-token/token/{token}")
    @ApiOperation("根据令牌将用户踢下线")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Void> signOutByToken(@PathVariable String token) {
        userTokenManager.signOutByToken(token);
        return ok();
    }

    @PutMapping("/user-token/user/{userId}/{state}")
    @ApiOperation("根据用户id更新用户令牌状态")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Void> changeUserState(@PathVariable String userId, @PathVariable TokenState state) {

        userTokenManager.changeUserState(userId, state);
        return ok();
    }

    @PutMapping("/user-token/token/{token}/{state}")
    @ApiOperation("根据令牌更新用户令牌状态")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Void> changeTokenState(@PathVariable String token, @PathVariable TokenState state) {
        userTokenManager.changeTokenState(token, state);
        return ok();
    }

    @PostMapping("/user-token/{token}/{type}/{userId}/{maxInactiveInterval}")
    @ApiOperation("将用户设置为登录")
    @Authorize(action = Permission.ACTION_ADD)
    public ResponseMessage<UserToken> signIn(@PathVariable String token, @PathVariable String type, @PathVariable String userId, @PathVariable long maxInactiveInterval) {
        return ok(userTokenManager.signIn(token, type, userId, maxInactiveInterval));
    }

    @GetMapping("/user-token/{token}/touch")
    @ApiOperation("更新token有效期")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Void> touch(@PathVariable String token) {
        userTokenManager.touch(token);
        return ok();
    }

    @GetMapping("/user-auth/{userId}")
    @ApiOperation("根据用户id获取用户的权限信息")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<Authentication> userAuthInfo(@PathVariable String userId) {
        return ok(authenticationManager.getByUserId(userId));
    }

}
