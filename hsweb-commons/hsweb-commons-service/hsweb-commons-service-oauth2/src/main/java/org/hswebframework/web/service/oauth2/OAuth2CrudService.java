package org.hswebframework.web.service.oauth2;

public interface OAuth2CrudService<E, PK> extends OAuth2QueryService<E, PK>
        , OAuth2QueryByEntityService<E>
        , OAuth2DeleteService<E,PK>
        , OAuth2InsertService<E, PK>
        , OAuth2UpdateService<E, PK> {
}
