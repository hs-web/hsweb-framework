package org.hswebframework.web.service.oauth2;

import org.hswebframework.web.service.DeleteService;

public interface OAuth2DeleteService<E, PK> extends DeleteService<E, PK>, OAuth2ServiceSupport {
    @Override
    default E deleteByPk(PK pk) {
        return createRequest("/" + pk).delete().as(getEntityType());
    }
}
