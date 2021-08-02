package org.hswebframework.web.oauth2.server.credential;

import lombok.Getter;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.OAuth2Request;

import java.util.Map;

@Getter
public class ClientCredentialRequest extends OAuth2Request {

    private final OAuth2Client client;

    public ClientCredentialRequest(OAuth2Client client, Map<String, String> parameters) {
        super(parameters);
        this.client = client;
    }
}
