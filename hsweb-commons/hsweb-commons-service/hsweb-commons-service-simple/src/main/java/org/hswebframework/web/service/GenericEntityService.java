/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
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
import org.hswebframework.web.commons.entity.LogicalDeleteEntity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;
import org.hswebframework.web.commons.entity.RecordModifierEntity;
import org.hswebframework.web.commons.entity.events.EntityCreatedEvent;
import org.hswebframework.web.commons.entity.events.EntityModifyEvent;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.validator.group.CreateGroup;
import org.hswebframework.web.validator.group.UpdateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
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

    protected ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostConstruct
    public void init() {
        if (logicPrimaryKeyValidator instanceof DefaultLogicPrimaryKeyValidator) {
            DefaultLogicPrimaryKeyValidator.registerQuerySuppiler(getEntityInstanceType(), bean -> this.createQuery().not("id", bean.getId()));
        }
    }

    @Override
    public E deleteByPk(PK pk) {
        E old = selectByPk(pk);
        if (old == null) {
            return null;
        }
        if (old instanceof LogicalDeleteEntity) {
            LogicalDeleteEntity deleteEntity = (LogicalDeleteEntity) old;
            deleteEntity.setDeleted(true);
            deleteEntity.setDeleteTime(System.currentTimeMillis());
            createUpdate()
                    .set(deleteEntity::getDeleted)
                    .set(deleteEntity::getDeleteTime)
                    .where(GenericEntity.id, pk)
                    .exec();
        } else {
            if (!physicalDeleteByPk(pk)) {
                logger.warn("物理删除数据失败,主键:{}", pk);
            }
        }
        return old;
    }

    protected boolean physicalDeleteByPk(PK pk) {
        //createDelete().where(GenericEntity.id,pk).exec()>0;

        return getDao().deleteByPk(pk) > 0;
    }

    protected boolean pushModifyEvent() {
        return RecordModifierEntity.class.isAssignableFrom(entityType);
    }

    protected boolean pushCreatedEvent() {
        return RecordCreationEntity.class.isAssignableFrom(entityType);
    }

    @Override
    public int updateByPk(PK pk, E entity) {
        Assert.notNull(pk, "primary key can not be null");
        Assert.hasText(String.valueOf(pk), "primary key can not be null");
        Assert.notNull(entity, "entity can not be null");
        entity.setId(pk);

        tryValidate(entity, UpdateGroup.class);
        //尝试推送 EntityModifyEvent 事件.
        if (eventPublisher != null && pushModifyEvent()) {
            E old = selectByPk(pk);
            eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, new EntityModifyEvent<>(old, entity, getEntityType()), getEntityType()));
        }
        return createUpdate(entity)
                //如果是RecordCreationEntity则不修改creator_id和creator_time
                .when(entity instanceof RecordCreationEntity,
                        update -> update.and().excludes(((RecordCreationEntity) entity).getCreatorIdProperty(), RecordCreationEntity.createTime))
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
        if (!StringUtils.isEmpty(entity.getId())) {
            if ((entity.getId() instanceof String) && !StringUtils.isEmpty(entity.getId())) {
                tryValidateProperty(entity.getId().toString().matches("[a-zA-Z0-9_\\-]+"), "id", "只能由数字,字母,下划线,和-组成");
            }
            tryValidateProperty(selectByPk(entity.getId()) == null, "id", entity.getId() + "已存在");
        }
        if (StringUtils.isEmpty(entity.getId()) && getIDGenerator() != null) {
            entity.setId(getIDGenerator().generate());
        }
        if (entity instanceof RecordCreationEntity) {
            ((RecordCreationEntity) entity).setCreateTimeNow();
        }
        tryValidate(entity, CreateGroup.class);
        getDao().insert(entity);

        if (eventPublisher != null && pushCreatedEvent()) {
            eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, new EntityCreatedEvent<>(entity, getEntityType()), getEntityType()));
        }
        return entity.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public E selectByPk(PK pk) {
        if (StringUtils.isEmpty(pk)) {
            return null;
        }
        return createQuery().where(GenericEntity.id, pk).single();
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> selectByPk(List<PK> id) {
        if (CollectionUtils.isEmpty(id)) {
            return new ArrayList<>();
        }
        return createQuery().where().in(GenericEntity.id, id).listNoPaging();
    }

}
