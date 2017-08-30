package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class WebUserTokenInterceptor extends HandlerInterceptorAdapter {

    private UserTokenManager userTokenManager;

    private List<UserTokenParser> userTokenParser;

    public WebUserTokenInterceptor(UserTokenManager userTokenManager, List<UserTokenParser> userTokenParser) {
        this.userTokenManager = userTokenManager;
        this.userTokenParser = userTokenParser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = userTokenParser.stream()
                .map(parser->parser.parseToken(request))
                .filter(Objects::nonNull)
                .filter(userTokenManager::tokenIsLoggedIn)
                .findFirst()
                .orElse(null);

        if (null == token) {
            return true;
        }
        userTokenManager.touch(token);
        UserToken userToken = userTokenManager.getByToken(token);
        if (userToken == null) {
            return true;
        } else {
            UserTokenHolder.setCurrent(userToken);
        }
        return true;
    }
}
