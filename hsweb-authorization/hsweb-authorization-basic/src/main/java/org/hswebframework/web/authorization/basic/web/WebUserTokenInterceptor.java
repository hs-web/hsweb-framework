package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenHolder;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户令牌拦截器,用于拦截用户请求并从中解析用户令牌信息
 *
 * @author zhouhao
 */
public class WebUserTokenInterceptor extends HandlerInterceptorAdapter {

    private final UserTokenManager userTokenManager;

    private final List<UserTokenParser> userTokenParser;

    private final AopMethodAuthorizeDefinitionParser parser;

    private final boolean enableBasicAuthorization;

    public WebUserTokenInterceptor(UserTokenManager userTokenManager,
                                   List<UserTokenParser> userTokenParser,
                                   AopMethodAuthorizeDefinitionParser definitionParser) {
        this.userTokenManager = userTokenManager;
        this.userTokenParser = userTokenParser;
        this.parser = definitionParser;

        enableBasicAuthorization = userTokenParser
                .stream()
                .filter(UserTokenForTypeParser.class::isInstance)
                .anyMatch(parser -> "basic".equalsIgnoreCase(((UserTokenForTypeParser) parser).getTokenType()));
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        List<ParsedToken> tokens = userTokenParser
                .stream()
                .map(parser -> parser.parseToken(request))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (tokens.isEmpty()) {
            if (enableBasicAuthorization && handler instanceof HandlerMethod) {
                HandlerMethod method = ((HandlerMethod) handler);
                AuthorizeDefinition definition = parser.parse(method.getBeanType(), method.getMethod());
                if (null != definition) {
                    response.addHeader("WWW-Authenticate", " Basic realm=\"\"");
                }
            }
            return true;
        }
        for (ParsedToken parsedToken : tokens) {
            UserToken userToken = null;
            String token = parsedToken.getToken();
            if (userTokenManager.tokenIsLoggedIn(token).blockOptional().orElse(false)) {
                userToken = userTokenManager.getByToken(token).blockOptional().orElse(null);
            }
            if ((userToken == null || userToken.isExpired()) && parsedToken instanceof AuthorizedToken) {
                //先踢出旧token
                userTokenManager.signOutByToken(token).subscribe();

                userToken = userTokenManager
                        .signIn(parsedToken.getToken(), parsedToken.getType(), ((AuthorizedToken) parsedToken).getUserId(), ((AuthorizedToken) parsedToken)
                                .getMaxInactiveInterval())
                        .block();
            }
            if (null != userToken) {
                userTokenManager.touch(token).subscribe();
                UserTokenHolder.setCurrent(userToken);
            }
        }
        return true;
    }

}
