package org.hswebframework.web.oauth2;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.oauth2.server.event.OAuth2GrantEvent;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.springframework.context.event.EventListener;

/**
 * @author zhouhao
 * @since 1.0
 */
@AllArgsConstructor
public class OAuth2GrantEventListener {

    private UserTokenManager userTokenManager;

    @EventListener
    public void handleOAuth2GrantEvent(OAuth2GrantEvent event) {
        userTokenManager.signIn(
                event.getAccessToken().getAccessToken(),
                "oauth2-access-token",
                event.getAccessToken().getOwnerId(),
                event.getAccessToken().getExpiresIn() * 1000L);

    }
}
