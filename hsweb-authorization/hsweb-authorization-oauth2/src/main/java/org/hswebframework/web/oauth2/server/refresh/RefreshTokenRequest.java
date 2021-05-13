package org.hswebframework.web.oauth2.server.refresh;

import lombok.Getter;
import org.hswebframework.web.oauth2.OAuth2Constants;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.OAuth2Request;

import java.util.Map;
import java.util.Optional;

@Getter
public class RefreshTokenRequest extends OAuth2Request {
    private final OAuth2Client client;

    public RefreshTokenRequest(OAuth2Client client, Map<String, String> parameters) {
        super(parameters);
        this.client = client;
    }

    public Optional<String> refreshToken(){
        return getParameter(OAuth2Constants.refresh_token);
    }
}
