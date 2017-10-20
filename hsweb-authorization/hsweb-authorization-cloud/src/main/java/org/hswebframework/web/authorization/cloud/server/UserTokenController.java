package org.hswebframework.web.authorization.cloud.server;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
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

    @GetMapping("/user-token/token/{token}")
    public UserToken getByToken(@PathVariable String token) {
        return userTokenManager.getByToken(token);
    }

    @GetMapping("/user-token/user/{userId}")
    public List<UserToken> getByUserId(@PathVariable String userId) {
        return userTokenManager.getByUserId(userId);
    }

    @GetMapping("/user-token/user/{userId}/logged")
    public boolean userIsLoggedIn(@PathVariable String userId) {
        return userTokenManager.userIsLoggedIn(userId);
    }

    @GetMapping("/user-token/token/{token}/logged")
    public boolean tokenIsLoggedIn(@PathVariable String token) {
        return userTokenManager.tokenIsLoggedIn(token);
    }

    @GetMapping("/user-token/user/total")
    public long totalUser() {
        return userTokenManager.totalUser();
    }

    @GetMapping("/user-token/token/total")
    public long totalToken() {
        return userTokenManager.totalToken();
    }

    @GetMapping("/user-token}")
    public List<UserToken> allLoggedUser() {
        return userTokenManager.allLoggedUser();
    }

    @DeleteMapping("/user-token/user/{userId}")
    public void signOutByUserId(@PathVariable String userId) {
        userTokenManager.signOutByUserId(userId);
    }

    @DeleteMapping("/user-token/token/{token}")
    public void signOutByToken(@PathVariable String token) {
        userTokenManager.signOutByToken(token);
    }

    @PutMapping("/user-token/user/{userId}/{state}")
    public void changeUserState(@PathVariable String userId, @PathVariable TokenState state) {
        userTokenManager.changeUserState(userId, state);
    }

    @PutMapping("/user-token/token/{token}/{state}")
    public void changeTokenState(String token, TokenState state) {
        userTokenManager.changeTokenState(token, state);
    }

    @PostMapping("/user-token/{token}/{type}/{userId}/{maxInactiveInterval}")
    public UserToken signIn(@PathVariable String token, @PathVariable String type, @PathVariable String userId, @PathVariable long maxInactiveInterval) {
        return userTokenManager.signIn(token, type, userId, maxInactiveInterval);
    }

    @GetMapping("/user-token/{token}/touch")
    public void touch(@PathVariable String token) {
        userTokenManager.touch(token);
    }

    @GetMapping("/user-auth/{userId}")
    public Authentication userAuthInfo(@PathVariable String userId) {
        return authenticationManager.getByUserId(userId);
    }

}
