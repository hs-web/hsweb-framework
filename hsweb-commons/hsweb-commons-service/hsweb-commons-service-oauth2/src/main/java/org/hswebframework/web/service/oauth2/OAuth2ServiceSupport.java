package org.hswebframework.web.service.oauth2;


import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Request;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;

public interface OAuth2ServiceSupport {

    OAuth2RequestService getRequestService();

    String getServiceId();

    String getUriPrefix();

   <E> Class<E> getEntityType();

    default OAuth2Session createSession() {
        return getRequestService().create(getServiceId()).byClientCredentials();
    }

    default OAuth2Request createRequest(String uri) {
        return createSession().request(getUriPrefix()+uri);
    }
}
