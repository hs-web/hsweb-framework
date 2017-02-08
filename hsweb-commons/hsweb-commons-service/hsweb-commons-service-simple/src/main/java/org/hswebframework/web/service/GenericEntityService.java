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
import org.hswebframework.web.commons.entity.RecordCreationEntity;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframwork.utils.ClassUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Transactional(rollbackFor = Throwable.class)
public abstract class GenericEntityService<E extends GenericEntity<PK>, PK>
        extends AbstractService<E, PK>
        implements GenericService<E, PK> {

    @SuppressWarnings("unchecked")
    public GenericEntityService() {
        super();
    }

    @Override
    public abstract CrudDao<E, PK> getDao();

    @Override
    public int deleteByPk(PK pk) {
        return createDelete()
                .where(GenericEntity.id, pk)
                .exec();
    }

    @Override
    public int updateByPk(E entity) {
        tryValidate(entity);
        return createUpdate(entity)
                //如果是RecordCreationEntity则不修改creator_id和creator_time
                .when(ClassUtils.instanceOf(getEntityType(), RecordCreationEntity.class),
                        update -> update.and().excludes("creator_id", "creator_time"))
                .where(GenericEntity.id, entity.getId())
                .exec();
    }

    @Override
    public int updateByPk(List<E> data) {
        return data.stream()
                .map(this::updateByPk)
                .reduce(Math::addExact)
                .orElse(0);
    }

    @Override
    public int saveOrUpdate(E entity) {
        if (null != entity.getId() && null != selectByPk(entity.getId())) {
            return updateByPk(entity);
        } else {
            insert(entity);
        }
        return 1;
    }

    @Override
    public PK insert(E entity) {
        tryValidate(entity);
        getDao().insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public E selectByPk(PK pk) {
        if (null == pk) return null;
        return createQuery().where(GenericEntity.id, pk).single();
    }

}
