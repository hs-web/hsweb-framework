package org.hswebframework.web.oauth2.server.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.event.DefaultAsyncEvent;
import org.hswebframework.web.oauth2.server.AccessToken;
import org.hswebframework.web.oauth2.server.OAuth2Client;

import java.util.Map;

/**
 * OAuth2授权成功事件
 *
 * @author zhouhao
 * @since 4.0.15
 */
@Getter
@AllArgsConstructor
public class OAuth2GrantedEvent extends DefaultAsyncEvent {
    private final OAuth2Client client;

    private final AccessToken accessToken;

    private final Authentication authentication;

    private final String scope;

    private final String grantType;

    private final Map<String, String> parameters;
}
