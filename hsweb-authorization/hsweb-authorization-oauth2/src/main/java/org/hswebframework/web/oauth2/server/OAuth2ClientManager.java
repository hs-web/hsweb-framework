package org.hswebframework.web.oauth2.server;

import reactor.core.publisher.Mono;

public interface OAuth2ClientManager {

    Mono<OAuth2Client> getClient(String clientId);

}
