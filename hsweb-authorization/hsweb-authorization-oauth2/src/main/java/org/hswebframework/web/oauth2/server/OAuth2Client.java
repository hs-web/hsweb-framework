package org.hswebframework.web.oauth2.server;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class OAuth2Client {

    @NotBlank
    private String clientId;

    @NotBlank
    private String clientSecret;

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String redirectUrl;

    //client 所属用户
    private String userId;

    public void validateRedirectUri(String redirectUri){

    }

}
