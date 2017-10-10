package org.hswebframework.web.authorization.cloud.feign;

import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zhouhao
 * @since
 */
@FeignClient(name = "${hsweb.cloud.user-center.name:user-center}")
public interface FeignUserTokenManager extends UserTokenManager {

    @Override
    @GetMapping("/user-token/token/{token}")
    UserToken getByToken(@PathVariable String token);

    @Override
    @GetMapping("/user-token/user/{userId}")
    List<UserToken> getByUserId(@PathVariable String userId);

    @Override
    @GetMapping("/user-token/user/{userId}/logged")
    boolean userIsLoggedIn(@PathVariable String userId);

    @Override
    @GetMapping("/user-token/token/{token}/logged")
    boolean tokenIsLoggedIn(@PathVariable String token);

    @Override
    @GetMapping("/user-token/user/total")
    long totalUser();

    @Override
    @GetMapping("/user-token/token/total")
    long totalToken();

    @Override
    @GetMapping("/user-token}")
    List<UserToken> allLoggedUser();

    @Override
    @DeleteMapping("/user-token/user/{userId}")
    void signOutByUserId(@PathVariable String userId);

    @Override
    @DeleteMapping("/user-token/token/{token}")
    void signOutByToken(@PathVariable String token);

    @Override
    @PutMapping("/user-token/user/{userId}/{state}")
    void changeUserState(@PathVariable String userId, @PathVariable TokenState state);

    @Override
    @PutMapping("/user-token/token/{token}/{state}")
    void changeTokenState(String token, TokenState state);

    @Override
    @PostMapping("/user-token/{token}/{userId}/{maxInactiveInterval}")
    UserToken signIn(@PathVariable String token, @PathVariable String userId, @PathVariable long maxInactiveInterval);

    @Override
    @GetMapping("/user-token/{token}/touch")
    void touch(String token);
}
