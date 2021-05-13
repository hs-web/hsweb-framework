package org.hswebframework.web.oauth2.server.code;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.oauth2.OAuth2Constants;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.OAuth2Request;

import java.util.Map;
import java.util.Optional;


@Getter
@Setter
public class AuthorizationCodeTokenRequest extends OAuth2Request {

    private OAuth2Client client;

    public AuthorizationCodeTokenRequest(OAuth2Client client, Map<String, String> parameters) {
        super(parameters);
        this.client = client;
    }

    public Optional<String> code() {
        return getParameter(OAuth2Constants.code).map(String::valueOf);
    }

    public Optional<String> scope() {
        return getParameter(OAuth2Constants.scope).map(String::valueOf);
    }
}
