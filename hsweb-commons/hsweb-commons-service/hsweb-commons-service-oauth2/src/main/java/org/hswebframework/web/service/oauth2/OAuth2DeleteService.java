package org.hswebframework.web.service.oauth2;

import org.hswebframework.web.service.DeleteService;
import org.hswebframework.web.service.InsertService;

public interface OAuth2DeleteService<PK> extends DeleteService<PK>, OAuth2ServiceSupport {
    @Override
    default int deleteByPk(PK pk) {
        return createRequest("/" + pk).delete().as(Integer.class);
    }
}
