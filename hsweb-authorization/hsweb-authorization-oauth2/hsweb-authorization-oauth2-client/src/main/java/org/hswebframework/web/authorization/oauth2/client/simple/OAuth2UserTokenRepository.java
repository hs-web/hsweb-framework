package org.hswebframework.web.authorization.oauth2.client.simple;

import org.hswebframework.web.authorization.oauth2.client.AccessTokenInfo;

import java.util.List;

/**
 * @author zhouhao
 * @since
 */
public interface OAuth2UserTokenRepository {
    AccessTokenInfo createToken();

    List<AccessTokenInfo> findByServerIdAndGrantType(String serverId, String grantType);

    AccessTokenInfo findByAccessToken(String accessToken);

    AccessTokenInfo update(String id, AccessTokenInfo tokenInfo);

    AccessTokenInfo insert(AccessTokenInfo accessTokenInfo);
}
