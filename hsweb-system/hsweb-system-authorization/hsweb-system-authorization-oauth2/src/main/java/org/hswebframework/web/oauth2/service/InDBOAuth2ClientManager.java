package org.hswebframework.web.oauth2.service;

import lombok.AllArgsConstructor;
import org.hswebframework.web.oauth2.entity.OAuth2ClientEntity;
import org.hswebframework.web.oauth2.server.OAuth2Client;
import org.hswebframework.web.oauth2.server.OAuth2ClientManager;
import reactor.core.publisher.Mono;

@AllArgsConstructor
public class InDBOAuth2ClientManager implements OAuth2ClientManager {

    private final OAuth2ClientService clientService;

    @Override
    public Mono<OAuth2Client> getClient(String clientId) {
        return clientService
                .findById(clientId)
                .filter(OAuth2ClientEntity::enabled)
                .map(OAuth2ClientEntity::toOAuth2Client);
    }
}
