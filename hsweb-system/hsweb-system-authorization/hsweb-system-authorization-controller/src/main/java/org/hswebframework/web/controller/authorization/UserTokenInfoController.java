package org.hswebframework.web.controller.authorization;

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
@AccessLogger("token信息")
@Authorize(permission = "user-token")
public class UserTokenInfoController {

    @Autowired
    private UserTokenManager userTokenManager;

    @GetMapping("/token/total")
    @Authorize(merge = false)
    public ResponseMessage<Long> allLoginToken() {
        return ResponseMessage.ok(userTokenManager.totalToken());
    }

    @GetMapping("/user/total")
    @Authorize(merge = false)
    public ResponseMessage<Long> allUserToken() {
        return ResponseMessage.ok(userTokenManager.totalUser());
    }

    @GetMapping("/reset")
    @Authorize(merge = false)
    public ResponseMessage<Boolean> resetToken() {
        UserToken token= UserTokenHolder.currentToken();
        if(token!=null){
            userTokenManager.signOutByToken(token.getToken());
        }
        return ResponseMessage.ok(true);
    }

    @GetMapping("/token/all")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<List<UserToken>> allTokenInfo() {
        return ResponseMessage.ok(userTokenManager.allLoggedUser());
    }

    @PutMapping("/token/{token}/{state}")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Void> changeTokenState(@PathVariable String token, @PathVariable TokenState state) {
        userTokenManager.changeTokenState(token,state);

        return ResponseMessage.ok();
    }
    @PutMapping("/user/{userId}/{state}")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<Void> changeUserState(@PathVariable String userId, @PathVariable TokenState state) {
        userTokenManager.changeUserState(userId,state);
        return ResponseMessage.ok();
    }

}
