package org.hswebframework.web.authorization.oauth2.client.simple;

import org.hswebframework.web.authorization.oauth2.client.OAuth2ServerConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0
 */
public class MemoryOAuth2ServerConfigRepository implements OAuth2ServerConfigRepository {
    private Map<String, OAuth2ServerConfig> list = new HashMap<>();

    @Override
    public OAuth2ServerConfig findById(String id) {
        return list.get(id);
    }

    public void setList(Map<String, OAuth2ServerConfig> list) {
        this.list = list;
    }

    public Map<String, OAuth2ServerConfig> getList() {
        return list;
    }
}
