package org.hswebframework.web.oauth2.service;

import org.hswebframework.web.crud.service.GenericReactiveCacheSupportCrudService;
import org.hswebframework.web.oauth2.entity.OAuth2ClientEntity;

public class OAuth2ClientService extends GenericReactiveCacheSupportCrudService<OAuth2ClientEntity, String> {

    @Override
    public String getCacheName() {
        return "oauth2-client";
    }
}
