package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.basic.aop.AopMethodAuthorizeDefinitionParser;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.token.ParsedToken;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenHolder;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.web.method.HandlerMethod;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 用户令牌拦截器,用于拦截用户请求并从中解析用户令牌信息
 *
 * @author zhouhao
 */
public class WebUserTokenInterceptor implements HandlerInterceptor {

    static final String TOKEN_ATTR = WebUserTokenInterceptor.class.getName() + ".token";

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
            .toList();

        if (tokens.isEmpty()) {
            if (enableBasicAuthorization && handler instanceof HandlerMethod method) {
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
                userToken =
                    userTokenManager
                        .signOutByToken(token)
                        .then(
                            userTokenManager
                                .signIn(parsedToken.getToken(),
                                        parsedToken.getType(),
                                        ((AuthorizedToken) parsedToken).getUserId(),
                                        ((AuthorizedToken) parsedToken)
                                            .getMaxInactiveInterval())
                        )

                        .block();
            }
            if (null != userToken) {
                userTokenManager.touch(token).subscribe();
                request.setAttribute(
                    TOKEN_ATTR, UserTokenHolder.makeCurrent(userToken)
                );
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                @Nonnull HttpServletResponse response,
                                @Nonnull Object handler,
                                Exception ex) throws Exception {
        Object closable = request.getAttribute(TOKEN_ATTR);
        if (closable instanceof Closeable c) {
            c.close();
        }
    }
}
