package org.hswebframework.web.service.oauth2;

import org.hswebframework.utils.ClassUtils;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.service.CreateEntityService;
import org.springframework.beans.factory.annotation.Autowired;

@SuppressWarnings("unchecked")
public abstract class AbstractOAuth2CrudService<E, PK> implements CreateEntityService<E>, OAuth2CrudService<E, PK> {

    private Class<E> entityType;

    private Class<PK> primaryKeyType;

    private OAuth2RequestService oAuth2RequestService;

    private EntityFactory entityFactory;

    public AbstractOAuth2CrudService() {
        entityType = (Class) ClassUtils.getGenericType(this.getClass(), 0);
        primaryKeyType = (Class) ClassUtils.getGenericType(this.getClass(), 1);
    }

    @Override
    public E createEntity() {
        return entityFactory.newInstance(entityType);
    }

    @Override
    public Class<E> getEntityInstanceType() {
        return entityType;
    }

    @Override
    public OAuth2RequestService getRequestService() {
        return oAuth2RequestService;
    }

    @Override
    public Class<E> getEntityType() {
        return entityType;
    }

    @Override
    public Class<PK> getPrimaryKeyType() {
        return primaryKeyType;
    }

    @Autowired
    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @Autowired
    public void setoAuth2RequestService(OAuth2RequestService oAuth2RequestService) {
        this.oAuth2RequestService = oAuth2RequestService;
    }
}
