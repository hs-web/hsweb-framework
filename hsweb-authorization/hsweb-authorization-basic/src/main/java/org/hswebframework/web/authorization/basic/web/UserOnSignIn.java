package org.hswebframework.web.authorization.basic.web;

import org.hswebframework.web.authorization.events.AuthorizationEvent;
import org.hswebframework.web.authorization.events.AuthorizationSuccessEvent;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenHolder;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 监听授权成功事件,授权成功后,生成token并注册到{@link UserTokenManager}
 *
 * @author zhouhao
 * @see org.springframework.context.ApplicationEvent
 * @see AuthorizationEvent
 * @see UserTokenManager
 * @see UserTokenGenerator
 * @since 3.0
 */
public class UserOnSignIn {

    /**
     * 默认到令牌类型
     *
     * @see UserToken#getType()
     * @see SessionIdUserTokenGenerator#getSupportTokenType()
     */
    private String defaultTokenType = "sessionId";

    /**
     * 令牌管理器
     */
    private UserTokenManager userTokenManager;

    private List<UserTokenGenerator> userTokenGenerators = new ArrayList<>();

    public UserOnSignIn(UserTokenManager userTokenManager) {
        this.userTokenManager = userTokenManager;
    }

    public void setDefaultTokenType(String defaultTokenType) {
        this.defaultTokenType = defaultTokenType;
    }

    @Autowired(required = false)
    public void setUserTokenGenerators(List<UserTokenGenerator> userTokenGenerators) {
        this.userTokenGenerators = userTokenGenerators;
    }

    @EventListener
    public void onApplicationEvent(AuthorizationSuccessEvent event) {
        UserToken token = UserTokenHolder.currentToken();
        String tokenType = (String) event.getParameter("token_type").orElse(defaultTokenType);

        if (token != null) {
            //先退出已登陆的用户
            event.async(userTokenManager.signOutByToken(token.getToken()));
        }
        //创建token
        GeneratedToken newToken = userTokenGenerators.stream()
                .filter(generator -> generator.getSupportTokenType().equals(tokenType))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException(tokenType))
                .generate(event.getAuthentication());
        //登入
        event.async(userTokenManager.signIn(newToken.getToken(), newToken.getType(), event.getAuthentication().getUser().getId(), newToken.getTimeout()).then());

        //响应结果
        event.getResult().putAll(newToken.getResponse());

    }
}
