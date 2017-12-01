package org.hswebframework.web.authorization.oauth2.client.simple;

import org.hswebframework.web.authorization.oauth2.client.OAuth2ServerConfig;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface OAuth2ServerConfigRepository {
    OAuth2ServerConfig findById(String id);

    OAuth2ServerConfig save(OAuth2ServerConfig config);
}
