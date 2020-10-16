package org.hswebframework.web.oauth2.server.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.oauth2.server.AccessToken;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RedisAccessToken implements Serializable {

    private String clientId;

    private String accessToken;

    private String refreshToken;

    private long createTime;

    private Authentication authentication;

    private boolean singleton;

    public AccessToken toAccessToken(int expiresIn){
        AccessToken token=new AccessToken();
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiresIn(expiresIn);
        return token;
    }
}
