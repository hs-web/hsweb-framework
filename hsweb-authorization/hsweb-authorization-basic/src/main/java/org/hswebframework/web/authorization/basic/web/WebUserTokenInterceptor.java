package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class WebUserTokenInterceptor extends HandlerInterceptorAdapter {

    private UserTokenManager userTokenManager;

    private UserTokenParser userTokenParser;

    public WebUserTokenInterceptor(UserTokenManager userTokenManager, UserTokenParser userTokenParser) {
        this.userTokenManager = userTokenManager;
        this.userTokenParser = userTokenParser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = userTokenParser.parseToken(request, userTokenManager::tokenIsLoggedIn);
        if (null == token) {
            return true;
        }
        userTokenManager.touch(token);
        UserToken userToken = userTokenManager.getByToken(token);
        if (userToken == null) {
            return true;
        } else if (userToken.isEffective()) {
            UserTokenHolder.setCurrent(userToken);
        } else if (userToken.isExpired()) {
            // TODO: 17-8-16 发送登录超时的错误信息
            userTokenManager.signOutByToken(token);
        } else if (userToken.isOffline()) {
            // TODO: 17-8-16 发送已被踢出的错误信息
            userTokenManager.signOutByToken(token);
        }
        return true;
    }
}
