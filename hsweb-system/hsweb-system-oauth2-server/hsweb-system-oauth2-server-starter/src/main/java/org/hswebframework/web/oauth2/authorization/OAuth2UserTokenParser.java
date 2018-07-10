package org.hswebframework.web.oauth2.authorization;

import org.hswebframework.web.authorization.basic.web.AuthorizedToken;
import org.hswebframework.web.authorization.basic.web.ParsedToken;
import org.hswebframework.web.authorization.basic.web.UserTokenParser;
import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.token.AccessTokenService;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.oauth2.core.OAuth2Constants;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

public class OAuth2UserTokenParser implements UserTokenParser {

    public static final String token_type = "oauth2-access-token";

    private AccessTokenService accessTokenService;

    public OAuth2UserTokenParser(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    public void setAccessTokenService(AccessTokenService accessTokenService) {
        this.accessTokenService = accessTokenService;
    }

    @Override
    public ParsedToken parseToken(HttpServletRequest request) {
        if (request.getRequestURI().contains("oauth2") && request.getParameter(OAuth2Constants.grant_type) != null) {
            return null;
        }
        String accessToken = request.getHeader(OAuth2Constants.authorization);
        if (StringUtils.isEmpty(accessToken)) {
            accessToken = request.getParameter(OAuth2Constants.access_token);
        } else {
            String[] arr = accessToken.split("[ ]");
            if (arr.length > 1 && ("Bearer".equalsIgnoreCase(arr[0]) || "OAuth".equalsIgnoreCase(arr[0]))) {
                accessToken = arr[1];
            }
        }
        if (StringUtils.isEmpty(accessToken)) {
            return null;
        }
        OAuth2AccessToken auth2AccessToken = accessTokenService.getTokenByAccessToken(accessToken);
        if (auth2AccessToken == null) {
            throw new GrantTokenException(ErrorType.INVALID_TOKEN);
        }
        Long time = auth2AccessToken.getUpdateTime() != null ? auth2AccessToken.getUpdateTime() : auth2AccessToken.getCreateTime();
        if (System.currentTimeMillis() - time > auth2AccessToken.getExpiresIn() * 1000L) {
            throw new GrantTokenException(ErrorType.EXPIRED_TOKEN);
        }

        return new AuthorizedToken() {
            @Override
            public String getUserId() {
                return auth2AccessToken.getOwnerId();
            }

            @Override
            public String getToken() {
                return auth2AccessToken.getAccessToken();
            }

            @Override
            public String getType() {
                return token_type;
            }

            @Override
            public long getMaxInactiveInterval() {
                return auth2AccessToken.getExpiresIn() * 1000L;
            }
        };
    }
}
