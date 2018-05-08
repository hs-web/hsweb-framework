package org.hswebframework.web.authorization.web;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
@Authorize(permission = "user-token", description = "用户令牌信息管理")
@Api(tags = "用户令牌管理", value = "用户令牌管理")
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
    public boolean resetToken() {
        UserToken token = UserTokenHolder.currentToken();
        if (token != null) {
            userTokenManager.signOutByToken(token.getToken());
        }
        return true;
    }

    @PutMapping("/user-token/check")
    @ApiOperation("检查所有已过期的token并移除")
    @Authorize(action = Permission.ACTION_UPDATE)
    public boolean checkExpiredToken() {
        userTokenManager.checkExpiredToken();
        return true;
    }

    @GetMapping("/user-token/token/{token}")
    @ApiOperation("根据token获取令牌信息")
    @Authorize(action = Permission.ACTION_GET)
    public UserToken getByToken(@PathVariable String token) {
        return userTokenManager.getByToken(token);
    }

    @GetMapping("/user-token/user/{userId}")
    @ApiOperation("根据用户ID获取全部令牌信息")
    @Authorize(action = Permission.ACTION_GET)
    public List<UserToken> getByUserId(@PathVariable String userId) {
        return userTokenManager.getByUserId(userId);
    }

    @GetMapping("/user-token/user/{userId}/logged")
    @ApiOperation("根据用户ID判断用户是否已经登录")
    @Authorize(action = Permission.ACTION_GET)
    public boolean userIsLoggedIn(@PathVariable String userId) {
        return userTokenManager.userIsLoggedIn(userId);
    }

    @GetMapping("/user-token/token/{token}/logged")
    @ApiOperation("根据令牌判断用户是否已经登录")
    @Authorize(action = Permission.ACTION_GET)
    public boolean tokenIsLoggedIn(@PathVariable String token) {
        return userTokenManager.tokenIsLoggedIn(token);
    }

    @GetMapping("/user-token/user/total")
    @ApiOperation("获取当前已经登录的用户数量")
    @Authorize(action = Permission.ACTION_GET)
    public long totalUser() {
        return userTokenManager.totalUser();
    }

    @GetMapping("/user-token/token/total")
    @ApiOperation("获取当前已经登录的令牌数量")
    @Authorize(action = Permission.ACTION_GET)
    public long totalToken() {
        return userTokenManager.totalToken();
    }

    @GetMapping("/user-token")
    @ApiOperation("获取全部用户令牌信息")
    @Authorize(action = Permission.ACTION_GET)
    public List<UserToken> allLoggedUser() {
        return userTokenManager.allLoggedUser();
    }

    @DeleteMapping("/user-token/user/{userId}")
    @ApiOperation("根据用户id将用户踢下线")
    @Authorize(action = Permission.ACTION_UPDATE)
    public void signOutByUserId(@PathVariable String userId) {
        userTokenManager.signOutByUserId(userId);
    }

    @DeleteMapping("/user-token/token/{token}")
    @ApiOperation("根据令牌将用户踢下线")
    @Authorize(action = Permission.ACTION_UPDATE)
    public void signOutByToken(@PathVariable String token) {
        userTokenManager.signOutByToken(token);
    }

    @PutMapping("/user-token/user/{userId}/{state}")
    @ApiOperation("根据用户id更新用户令牌状态")
    @Authorize(action = Permission.ACTION_UPDATE)
    public void changeUserState(@PathVariable String userId, @PathVariable TokenState state) {
        userTokenManager.changeUserState(userId, state);
    }

    @PutMapping("/user-token/token/{token}/{state}")
    @ApiOperation("根据令牌更新用户令牌状态")
    @Authorize(action = Permission.ACTION_UPDATE)
    public void changeTokenState(@PathVariable String token, @PathVariable TokenState state) {
        userTokenManager.changeTokenState(token, state);
    }

    @PostMapping("/user-token/{token}/{type}/{userId}/{maxInactiveInterval}")
    @ApiOperation("将用户设置为登录")
    @Authorize(action = Permission.ACTION_ADD)
    public UserToken signIn(@PathVariable String token, @PathVariable String type, @PathVariable String userId, @PathVariable long maxInactiveInterval) {
        return userTokenManager.signIn(token, type, userId, maxInactiveInterval);
    }

    @GetMapping("/user-token/{token}/touch")
    @ApiOperation("更新token有效期")
    @Authorize(action = Permission.ACTION_UPDATE)
    public void touch(@PathVariable String token) {
        userTokenManager.touch(token);
    }

    @GetMapping("/user-auth/{userId}")
    @ApiOperation("根据用户id获取用户的权限信息")
    @Authorize(action = Permission.ACTION_GET)
    public Authentication userAuthInfo(@PathVariable String userId) {
        return authenticationManager.getByUserId(userId);
    }

}
