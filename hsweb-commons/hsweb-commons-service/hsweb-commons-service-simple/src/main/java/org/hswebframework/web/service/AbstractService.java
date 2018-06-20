package org.hswebframework.web.service;

import org.hswebframework.utils.ClassUtils;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.validate.SimpleValidateResults;
import org.hswebframework.web.validate.ValidationException;
import org.hswebframework.web.validator.LogicPrimaryKeyValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 抽象服务类,提供通用模板方法、类,如验证器,实体工厂等
 *
 * @author zhouhao
 * @see CreateEntityService
 * @see Service
 */
public abstract class AbstractService<E extends Entity, PK> implements CreateEntityService<E>, Service {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    protected Validator validator;

    protected EntityFactory entityFactory;

    protected LogicPrimaryKeyValidator logicPrimaryKeyValidator;

    @Autowired(required = false)
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Autowired(required = false)
    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @Autowired(required = false)
    public void setLogicPrimaryKeyValidator(LogicPrimaryKeyValidator logicPrimaryKeyValidator) {
        this.logicPrimaryKeyValidator = logicPrimaryKeyValidator;
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

    @Override
    public Class<E> getEntityInstanceType() {
        return entityFactory.getInstanceType(getEntityType());
    }

    public Class<E> getEntityType() {
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

    protected void tryValidate(Object data, String property, Class... groups) {
        validate(() -> validator.validateProperty(data, property, groups));
    }

    protected <T> void tryValidate(Class<T> type, String property, Object value, Class... groups) {
        validate(() -> validator.validateValue(type, property, value, groups));
    }

    protected void tryValidate(Object data, Class... groups) {
        validate(() -> validator.validate(data, groups));
    }

    private <T> void validate(Supplier<Set<ConstraintViolation<T>>> validatorSetFunction) {
        if (validator == null) {
            logger.warn("validator is null!");
            return;
        }
        SimpleValidateResults results = new SimpleValidateResults();
        validatorSetFunction.get()
                .forEach(violation -> results.addResult(violation.getPropertyPath().toString(), violation.getMessage()));
        if (!results.isSuccess()) {
            throw new ValidationException(results);
        }
    }

    public static void assertNotNull(Object data) {
        assertNotNull(data, "{data_not_found}");
    }

    public static void assertNotNull(Object data, String message) {
        if (null == data) {
            throw new NotFoundException(message);
        }
    }


}
