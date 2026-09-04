package org.hswebframework.web.oauth2.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * OAuth2 access-token response.
 *
 * <p>The legacy three-argument constructor remains available and now emits the standard Bearer
 * token type. Scope is optional and must describe the effective authorization actually granted by
 * the issuer.</p>
 *
 * @author zhouhao
 * @since 5.0.2
 */
@Getter
@Setter
@NoArgsConstructor
@ToString
public class AccessToken extends OAuth2Response {

    private static final long serialVersionUID = -6849794470754667710L;

    public static final String DEFAULT_TOKEN_TYPE = "Bearer";

    @Schema(name="access_token")
    @JsonProperty("access_token")
    private String accessToken;

    @Schema(name="refresh_token")
    @JsonProperty("refresh_token")
    private String refreshToken;

    @Schema(name="expires_in")
    @JsonProperty("expires_in")
    private int expiresIn;

    @Schema(name = "token_type", defaultValue = DEFAULT_TOKEN_TYPE)
    @JsonProperty("token_type")
    private String tokenType = DEFAULT_TOKEN_TYPE;

    @Schema(name = "scope", description = "实际授予的OAuth2 scope，多个值使用空格分隔")
    @JsonProperty("scope")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String scope;

    /**
     * 保留已发布构造器，并为旧调用方补充标准 Bearer token type。
     */
    public AccessToken(String accessToken, String refreshToken, int expiresIn) {
        this(accessToken, refreshToken, expiresIn, DEFAULT_TOKEN_TYPE, null);
    }

    /**
     * Create an OAuth2 token response with explicit token type and effective scope.
     */
    public AccessToken(String accessToken,
                       String refreshToken,
                       int expiresIn,
                       String tokenType,
                       String scope) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.tokenType = tokenType;
        this.scope = scope;
    }

    public String getTokenType() {
        return tokenType == null || tokenType.isBlank()
            ? DEFAULT_TOKEN_TYPE
            : tokenType;
    }
}
