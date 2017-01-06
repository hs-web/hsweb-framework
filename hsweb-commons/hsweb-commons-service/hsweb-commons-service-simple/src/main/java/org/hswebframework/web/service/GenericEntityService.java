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

import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
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
public abstract class GenericEntityService<B extends GenericEntity<PK>, PK>
        extends AbstractService<B, PK>
        implements GenericService<B, PK> {

    @SuppressWarnings("unchecked")
    public GenericEntityService() {
        super();
    }

    @Override
    public abstract CrudDao<B, PK> getDao();

    @Override
    public int deleteByPk(PK pk) {
        return createDelete().where(GenericEntity.id, pk).exec();
    }

    @Override
    public int updateByPk(B data) {
        tryValidate(data);
        return createUpdate(data).where(GenericEntity.id, data.getId()).exec();
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
        tryValidateProperty(null != data.getId(), GenericEntity.id, "id {not_be_null}");
        tryValidate(data);
        getDao().insert(data);
        return data.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public B selectByPk(PK id) {
        return createQuery().where(GenericEntity.id, id).single();
    }
}
