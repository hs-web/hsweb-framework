package org.hswebframework.web.service.oauth2;


import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Request;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;

public interface OAuth2ServiceSupport {

    OAuth2RequestService getRequestService();

    String getServiceId();

    String getUriPrefix();

    <E> Class<E> getEntityType();

    <PK> Class<PK> getPrimaryKeyType();

    default OAuth2Session createSession() {
        return getRequestService().create(getServiceId()).byClientCredentials();
    }

    default OAuth2Request createRequest(String uri) {
        return createSession().request(getUriPrefix() + uri);
    }

    default OAuth2Request createRequest(String uri, Object param) {
        return createSession().request(getUriPrefix() + uri)
                .params(WebUtil.objectToHttpParameters(param));
    }
}
