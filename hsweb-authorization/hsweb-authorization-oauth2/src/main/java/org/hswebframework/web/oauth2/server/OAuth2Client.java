package org.hswebframework.web.oauth2.server;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.oauth2.ErrorType;
import org.hswebframework.web.oauth2.OAuth2Exception;
import org.springframework.util.StringUtils;

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

    public void validateRedirectUri(String redirectUri) {
        if (StringUtils.isEmpty(redirectUri) || (!redirectUri.startsWith(this.redirectUrl))) {
            throw new OAuth2Exception(ErrorType.ILLEGAL_REDIRECT_URI);
        }
    }

}
