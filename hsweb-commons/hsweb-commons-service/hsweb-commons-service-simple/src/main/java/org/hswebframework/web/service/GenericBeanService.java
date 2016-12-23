/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.service;

import org.hswebframework.web.commons.beans.GenericBean;
import org.hswebframework.web.commons.beans.factory.BeanFactory;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.validate.SimpleValidateResults;
import org.hswebframework.web.validate.ValidationException;
import org.hswebframwork.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Validator;
import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Transactional(rollbackFor = Throwable.class)
public abstract class GenericBeanService<B extends GenericBean<PK>, PK> implements GenericService<B, PK> {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired(required = false)
    private Validator validator;

    @Autowired(required = false)
    private BeanFactory beanFactory;

    private Class<B>  beanType;
    private Class<PK> primaryKeyType;

    @SuppressWarnings("unchecked")
    public GenericBeanService(){
        primaryKeyType = (Class<PK>) ClassUtils.getGenericType(this.getClass(), 1);
        beanType = (Class<B>) ClassUtils.getGenericType(this.getClass(), 0);
    }

    @Override
    public abstract CrudDao<B, PK> getDao();

    private boolean beanFactoryIsEnabled() {
        if (beanFactory == null) {
            logger.warn("beanFactory is null!");
        }
        return null != beanFactory;
    }

    protected Class<B> getBeanRealType() {
        return beanFactory.getInstanceType(getBeanType());
    }

    protected Class<B> getBeanType() {
        return beanType;
    }

    protected Class<PK> getPrimaryKeyType() {
        return primaryKeyType;
    }

    @Override
    public B createBean() {
        if (!beanFactoryIsEnabled()) {
            throw new UnsupportedOperationException("{unsupported_operation}");
        }
        return beanFactory.newInstance(getBeanType());
    }

    public void tryValidate(B bean) {
        if (validator == null) {
            logger.warn("validator is null!");
            return;
        }
        SimpleValidateResults results = new SimpleValidateResults();
        validator.validate(bean).forEach(violation -> results.addResult(violation.getPropertyPath().toString(), violation.getMessage()));
        if (!results.isSuccess())
            throw new ValidationException(results);
    }

    @Override
    public int deleteByPk(PK pk) {
        return createDelete().where(GenericBean.id, pk).exec();
    }

    @Override
    public int updateByPk(B data) {
        tryValidate(data);
        return createUpdate(data).where(GenericBean.id, data.getId()).exec();
    }

    @Override
    public int updateByPk(List<B> data) {
        return data.stream().map(this::updateByPk).reduce(Math::addExact).orElse(0);
    }

    @Override
    public int saveOrUpdate(B po) {
        if (null != po.getId()) {
            return updateByPk(po);
        } else {
            insert(po);
        }
        return 1;
    }

    @Override
    public PK insert(B data) {
        tryValidate(data);
        getDao().insert(data);
        return data.getId();
    }

    @Override
    public B selectByPk(PK id) {
        return createQuery().where(GenericBean.id, id).single();
    }

    public void assertNotNull(Object data) {
        assertNotNull(data, "{data_not_found}");
    }

    public void assertNotNull(Object data, String message) {
        if (null == data) throw new NotFoundException(message);
    }
}
