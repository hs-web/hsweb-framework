package org.hswebframework.web.authorization.oauth2.client.simple;

import org.hswebframework.web.authorization.oauth2.client.AccessTokenInfo;
import org.hswebframework.web.authorization.oauth2.client.simple.OAuth2UserTokenRepository;
import org.hswebframework.web.id.IDGenerator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since
 */
public class MemoryOAuth2UserTokenRepository implements OAuth2UserTokenRepository {

    private Map<String, AccessTokenInfo> accessTokenInfoRepo = new ConcurrentHashMap<>();

    @Override
    public AccessTokenInfo createToken() {
        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        tokenInfo.setId(IDGenerator.MD5.generate());
        return tokenInfo;
    }

    @Override
    public List<AccessTokenInfo> findByServerIdAndGrantType(String serverId, String grantType) {
        return accessTokenInfoRepo.values().stream().filter(token ->
                token.getServerId().equals(serverId) && token.getGrantType().equals(grantType)
        ).collect(Collectors.toList());
    }

    @Override
    public AccessTokenInfo findByAccessToken(String accessToken) {
        return accessTokenInfoRepo.values().stream().filter(token ->
                token.getAccessToken().equals(accessToken)
        ).findFirst().orElse(null);
    }

    @Override
    public AccessTokenInfo update(String id, AccessTokenInfo tokenInfo) {
        accessTokenInfoRepo.put(id, tokenInfo);
        return tokenInfo;
    }

    @Override
    public AccessTokenInfo insert(AccessTokenInfo accessTokenInfo) {
        accessTokenInfo.setCreateTime(System.currentTimeMillis());
        accessTokenInfo.setUpdateTime(System.currentTimeMillis());
        if (accessTokenInfo.getId() == null) {
            accessTokenInfo.setId(IDGenerator.MD5.generate());
        }
        accessTokenInfoRepo.put(accessTokenInfo.getId(), accessTokenInfo);
        return accessTokenInfo;
    }
}
