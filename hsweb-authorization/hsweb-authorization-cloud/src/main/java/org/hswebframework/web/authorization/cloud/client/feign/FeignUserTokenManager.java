package org.hswebframework.web.authorization.cloud.client.feign;

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
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/token/{token}", method = RequestMethod.GET)
    UserToken getByToken(@PathVariable("token") String token);

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/user/{userId}", method = RequestMethod.GET)
    List<UserToken> getByUserId(@PathVariable("userId") String userId);

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/user/{userId}/logged", method = RequestMethod.GET)
    boolean userIsLoggedIn(@PathVariable("userId") String userId);

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/token/{token}/logged", method = RequestMethod.GET)
    boolean tokenIsLoggedIn(@PathVariable("token") String token);

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/user/total", method = RequestMethod.GET)
    long totalUser();

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/token/total", method = RequestMethod.GET)
    long totalToken();

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token", method = RequestMethod.GET)
    List<UserToken> allLoggedUser();

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/user/{userId}", method = RequestMethod.DELETE)
    void signOutByUserId(@PathVariable("userId") String userId);

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/token/{token}", method = RequestMethod.DELETE)
    void signOutByToken(@PathVariable("token") String token);

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/user/{userId}/{state}", method = RequestMethod.PUT)
    void changeUserState(@PathVariable("userId") String userId, @PathVariable("state") TokenState state);

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/token/{token}/{state}", method = RequestMethod.PUT)
    void changeTokenState(@PathVariable("token") String token, @PathVariable("state") TokenState state);

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/{token}/{type}/{userId}/{maxInactiveInterval}", method = RequestMethod.POST)
    UserToken signIn(@PathVariable("token") String token, @PathVariable("type") String type, @PathVariable("userId") String userId, @PathVariable("maxInactiveInterval") long maxInactiveInterval);

    @Override
    @RequestMapping(value = "${hsweb.cloud.user-center.prefix:/}user-token/{token}/touch", method = RequestMethod.GET)
    void touch(@PathVariable("token") String token);
}
