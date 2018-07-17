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
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.validator.DuplicateKeyException;
import org.hswebframework.web.validator.LogicPrimaryKeyValidator;
import org.hswebframework.web.validator.group.CreateGroup;
import org.hswebframework.web.validator.group.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用实体服务类，提供增删改查的默认实现
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

    /**
     * 获取ID生成器,在insert的时候，如果ID为空,则调用生成器进行生成
     *
     * @return IDGenerator
     * @see IDGenerator
     */
    protected abstract IDGenerator<PK> getIDGenerator();

    @PostConstruct
    public void init() {
        if (null != logicPrimaryKeyValidator && logicPrimaryKeyValidator instanceof DefaultLogicPrimaryKeyValidator) {
            DefaultLogicPrimaryKeyValidator.registerQuerySuppiler(getEntityInstanceType(), bean -> this.createQuery().not("id", bean.getId()));
        }
    }

    @Override
    public E deleteByPk(PK pk) {
        Assert.notNull(pk, "parameter can not be null");
        E old = selectByPk(pk);
        getDao().deleteByPk(pk);
        return old;
    }

    @Override
    public int updateByPk(PK pk, E entity) {
        Assert.notNull(pk, "primary key can not be null");
        Assert.notNull(entity, "entity can not be null");
        entity.setId(pk);
        tryValidate(entity, UpdateGroup.class);

        return createUpdate(entity)
                //如果是RecordCreationEntity则不修改creator_id和creator_time
                .when(entity instanceof RecordCreationEntity,
                        update -> update.and().excludes(RecordCreationEntity.creatorId, RecordCreationEntity.createTime))
                .where(GenericEntity.id, pk)
                .exec();
    }

    protected int updateByPk(E entity) {
        return updateByPk(entity.getId(), entity);
    }

    @Override
    public int updateByPk(List<E> data) {
        return data.stream()
                .map(this::updateByPk)
                .reduce(Math::addExact)
                .orElse(0);
    }

    @Override
    public PK saveOrUpdate(E entity) {
        if (dataExisted(entity)) {
            updateByPk(entity);
        } else {
            insert(entity);
        }
        return entity.getId();
    }

    @SuppressWarnings("unchecked")
    protected boolean dataExisted(E entity) {
        if (null != logicPrimaryKeyValidator) {
            logicPrimaryKeyValidator
                    .validate(entity)
                    .ifError(result -> entity.setId(result.getData().getId()));
        }
        return null != entity.getId() && null != selectByPk(entity.getId());
    }

    @Override
    public PK insert(E entity) {
        if (entity.getId() != null) {
            if ((entity.getId() instanceof String) && !StringUtils.isEmpty(entity.getId())) {
                tryValidateProperty(entity.getId().toString().matches("[a-zA-Z0-9_\\-]+"), "id", "只能由数字,字母,下划线,和-组成");
            }
            tryValidateProperty(selectByPk(entity.getId()) == null, "id", entity.getId() + "已存在");
        }
        if (entity.getId() == null && getIDGenerator() != null) {
            entity.setId(getIDGenerator().generate());
        }
        if (entity instanceof RecordCreationEntity) {
            ((RecordCreationEntity) entity).setCreateTimeNow();
        }
        tryValidate(entity, CreateGroup.class);
        getDao().insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public E selectByPk(PK pk) {
        if (null == pk) {
            return null;
        }
        return createQuery().where(GenericEntity.id, pk).single();
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> selectByPk(List<PK> id) {
        if (id == null || id.isEmpty()) {
            return new ArrayList<>();
        }
        return createQuery().where().in(GenericEntity.id, id).listNoPaging();
    }

}
