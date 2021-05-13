package org.hswebframework.web.oauth2.server.code;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hswebframework.web.oauth2.OAuth2Constants;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.OAuth2Request;
import org.hswebframework.web.oauth2.server.OAuth2Response;

import java.util.HashMap;

@Getter
@Setter
@ToString
public class AuthorizationCodeResponse extends OAuth2Response {
    private String code;

    public AuthorizationCodeResponse(String code) {
        this.code = code;
        with(OAuth2Constants.code, code);
    }
}
