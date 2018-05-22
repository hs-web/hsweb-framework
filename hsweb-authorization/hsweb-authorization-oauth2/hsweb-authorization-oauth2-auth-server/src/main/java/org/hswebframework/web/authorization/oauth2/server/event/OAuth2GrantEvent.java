package org.hswebframework.web.authorization.oauth2.server.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;

/**
 * @author zhouhao
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public class OAuth2GrantEvent {
    private OAuth2AccessToken accessToken;
}
