package org.hswebframework.web.oauth2.server;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import jakarta.validation.constraints.NotBlank;

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

    public void validateRedirectUri(String redirectUri) {
        if (ObjectUtils.isEmpty(redirectUri) || (!redirectUri.startsWith(this.redirectUrl))) {
            throw new OAuth2Exception(ErrorType.ILLEGAL_REDIRECT_URI);
        }
    }

    public void validateSecret(String secret) {
        if (ObjectUtils.isEmpty(secret) || (!secret.equals(this.clientSecret))) {
            throw new OAuth2Exception(ErrorType.ILLEGAL_CLIENT_SECRET);
        }
    }

}
