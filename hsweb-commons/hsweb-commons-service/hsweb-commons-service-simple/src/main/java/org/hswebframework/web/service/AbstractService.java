package org.hswebframework.web.service;

import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.validate.SimpleValidateResults;
import org.hswebframework.web.validate.ValidationException;
import org.hswebframwork.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Validator;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public abstract class AbstractService<E extends Entity, PK> implements CreateEntityService<E>, Service {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Validator validator;

    protected EntityFactory entityFactory;

    @Autowired(required = false)
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Autowired(required = false)
    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    protected Class<E> entityType;

    protected Class<PK> primaryKeyType;

    @SuppressWarnings("unchecked")
    public AbstractService() {
        primaryKeyType = (Class<PK>) ClassUtils.getGenericType(this.getClass(), 1);
        entityType = (Class<E>) ClassUtils.getGenericType(this.getClass(), 0);
    }

    protected boolean entityFactoryIsEnabled() {
        if (entityFactory == null) {
            logger.warn("entityFactory is null!");
        }
        return null != entityFactory;
    }

    protected Class<E> getEntityRealType() {
        return entityFactory.getInstanceType(getEntityType());
    }

    protected Class<E> getEntityType() {
        return entityType;
    }

    protected Class<PK> getPrimaryKeyType() {
        return primaryKeyType;
    }

    @Override
    public E createEntity() {
        if (!entityFactoryIsEnabled()) {
            throw new UnsupportedOperationException("{unsupported_operation}");
        }
        return entityFactory.newInstance(getEntityType());
    }

    protected <T> void tryValidateProperty(org.hswebframework.web.service.Validator<T> validator, String property, T value) {
        if (validator != null) {
            if (!validator.validate(value)) {
                throw new ValidationException(validator.getErrorMessage(), property);
            }
        }
    }

    protected <T> void tryValidateProperty(org.hswebframework.web.service.Validator<T> validator, String property, T value, String message) {
        if (validator != null) {
            if (!validator.validate(value)) {
                throw new ValidationException(message, property);
            }
        }
    }

    protected void tryValidateProperty(boolean success, String property, String message) {
        if (!success) {
            throw new ValidationException(message, property);
        }
    }

    protected void tryValidate(E bean) {
        if (validator == null) {
            logger.warn("validator is null!");
            return;
        }
        SimpleValidateResults results = new SimpleValidateResults();
        validator.validate(bean).forEach(violation -> results.addResult(violation.getPropertyPath().toString(), violation.getMessage()));
        if (!results.isSuccess())
            throw new ValidationException(results);
    }

    public static void assertNotNull(Object data) {
        assertNotNull(data, "{data_not_found}");
    }

    public static void assertNotNull(Object data, String message) {
        if (null == data) throw new NotFoundException(message);
    }


}
