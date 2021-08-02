package org.hswebframework.web.oauth2.server.code;


import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.OAuth2Request;

import java.util.Map;

@Getter
@Setter
public class AuthorizationCodeRequest extends OAuth2Request {
    private OAuth2Client client;

    private Authentication authentication;


    public AuthorizationCodeRequest(OAuth2Client client,
                                    Authentication authentication,
                                    Map<String, String> parameters) {
        super(parameters);
        this.client = client;
        this.authentication = authentication;
    }
}
