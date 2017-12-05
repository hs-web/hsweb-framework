package org.hswebframework.web.controller.authorization;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenHolder;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${hsweb.web.mappings.user-token:user-token}")
@Api(value = "用户令牌", tags = "权限-用户令牌管理")
@Authorize(permission = "user-token", description = "用户令牌管理")
public class UserTokenInfoController {

    @Autowired
    private UserTokenManager userTokenManager;

    @GetMapping("/token/total")
    @Authorize(merge = false)
    @ApiOperation("获取已授权令牌的总数")
    public ResponseMessage<Long> allLoginToken() {
        return ResponseMessage.ok(userTokenManager.totalToken());
    }

    @GetMapping("/user/total")
    @Authorize(merge = false)
    @ApiOperation("获取已授权用户的总数")
    public ResponseMessage<Long> allUserToken() {
        return ResponseMessage.ok(userTokenManager.totalUser());
    }

    @GetMapping("/reset")
    @Authorize(merge = false)
    @ApiOperation("重置当前用户的令牌")
    public ResponseMessage<Boolean> resetToken() {
        UserToken token = UserTokenHolder.currentToken();
        if (token != null) {
            userTokenManager.signOutByToken(token.getToken());
        }
        return ResponseMessage.ok(true);
    }

    @GetMapping("/token/all")
    @ApiOperation("获取所有令牌")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<List<UserToken>> allTokenInfo() {
        return ResponseMessage.ok(userTokenManager.allLoggedUser());
    }

    @PutMapping("/token/{token}/{state}")
    @ApiOperation("修改令牌状态")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Void> changeTokenState(@ApiParam("令牌") @PathVariable String token, @ApiParam("要修改的状态") @PathVariable TokenState state) {
        userTokenManager.changeTokenState(token, state);

        return ResponseMessage.ok();
    }

    @PutMapping("/user/{userId}/{state}")
    @ApiOperation("修改用户状态")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<Void> changeUserState(@ApiParam("用户ID") @PathVariable String userId, @ApiParam("要修改的状态") @PathVariable TokenState state) {
        userTokenManager.changeUserState(userId, state);
        return ResponseMessage.ok();
    }

    @PutMapping("/check")
    @ApiOperation("检查所有已过期的token并移除")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Void> checkExpiredToken() {
        userTokenManager.checkExpiredToken();
        return ResponseMessage.ok();
    }

}
