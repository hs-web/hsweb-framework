package org.hswebframework.web.authorization.oauth2.server.client;

import org.hswebframework.web.id.IDGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryOAuth2ClientConfigRepository implements OAuth2ClientConfigRepository {
    private Map<String, OAuth2Client> clients = new HashMap<>();

    public void setClients(Map<String, OAuth2Client> clients) {
        this.clients = clients;
    }

    @Override
    public OAuth2Client getClientById(String id) {
        return clients.get(id);
    }

    @Override
    public OAuth2Client getClientByOwnerId(String ownerId) {
        return clients.values().stream().filter(client -> ownerId.equals(client.getOwnerId())).findFirst().orElse(null);
    }

    @Override
    public OAuth2Client save(OAuth2Client oAuth2Client) {
        clients.put(oAuth2Client.getId(), oAuth2Client);
        return oAuth2Client;
    }

    @Override
    public OAuth2Client newClient() {
        return SimpleOAuth2Client.builder()
                .id(IDGenerator.MD5.generate())
                .secret(IDGenerator.MD5.generate())
                .build();
    }

    @Override
    public OAuth2Client remove(String id) {
        return clients.remove(id);
    }

    @Override
    public List<OAuth2Client> getAll() {
        return new ArrayList<>(clients.values());
    }

}
