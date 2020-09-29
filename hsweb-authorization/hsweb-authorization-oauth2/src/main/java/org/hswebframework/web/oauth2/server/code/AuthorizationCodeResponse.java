package org.hswebframework.web.oauth2.server.code;


import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.OAuth2Request;
import org.hswebframework.web.oauth2.server.OAuth2Response;

import java.util.HashMap;

@Getter
@Setter
public class AuthorizationCodeResponse extends OAuth2Response {
    private String code;

    public AuthorizationCodeResponse(String code) {
        this.code = code;
        with("code", code);
    }
}
