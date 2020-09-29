package org.hswebframework.web.oauth2.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessToken extends OAuth2Response {

    @Schema(name="access_token")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(name="expires_in")
    @JsonProperty("expires_in")
    private int expiresIn;

    @Schema(name="refresh_token")
    @JsonProperty("refresh_token")
    private String refreshToken;

}
