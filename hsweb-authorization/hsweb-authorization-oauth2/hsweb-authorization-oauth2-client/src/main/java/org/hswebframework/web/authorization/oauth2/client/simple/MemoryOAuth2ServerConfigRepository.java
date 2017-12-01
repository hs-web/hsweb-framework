package org.hswebframework.web.authorization.oauth2.client.simple;

import org.hswebframework.web.authorization.oauth2.client.OAuth2ServerConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0
 */
public class MemoryOAuth2ServerConfigRepository implements OAuth2ServerConfigRepository {

    private Map<String, OAuth2ServerConfig> repo = new HashMap<>();

    private List<OAuth2ServerConfig> servers;

    @Override
    public OAuth2ServerConfig findById(String id) {
        return repo.get(id);
    }

    @Override
    public OAuth2ServerConfig save(OAuth2ServerConfig config) {
        repo.put(config.getId(), config);
        return config;
    }

    public void setServers(List<OAuth2ServerConfig> servers) {
        this.servers = servers;
        repo = servers.stream()
                .collect(Collectors.toMap(OAuth2ServerConfig::getId, Function.identity()));
    }

    public List<OAuth2ServerConfig> getServers() {
        return servers;
    }
}
