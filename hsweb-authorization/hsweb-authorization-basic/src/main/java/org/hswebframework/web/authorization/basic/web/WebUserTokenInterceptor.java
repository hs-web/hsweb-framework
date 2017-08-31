package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
        List<ParsedToken> tokens = userTokenParser.stream()
                .map(parser -> parser.parseToken(request))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (tokens.isEmpty()) {
            return true;
        }
        for (ParsedToken parsedToken : tokens) {
            UserToken userToken = null;
            String token = parsedToken.getToken();
            if (userTokenManager.tokenIsLoggedIn(token)) {
                userToken = userTokenManager.getByToken(token);
            }
//            if ((userToken == null || userToken.isExpired()) && parsedToken instanceof AuthorizedToken) {
//                userToken = userTokenManager.signIn(parsedToken.getToken(), ((AuthorizedToken) parsedToken).getUserId(), -1);
//            }
            if (null != userToken) {
                userTokenManager.touch(token);
                UserTokenHolder.setCurrent(userToken);
            }
        }
        return true;
    }
}
