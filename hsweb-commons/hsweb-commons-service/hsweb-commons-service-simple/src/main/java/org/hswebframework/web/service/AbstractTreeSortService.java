/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.service;

import org.hswebframework.utils.RandomUtil;
import org.hswebframework.web.commons.entity.TreeSortSupportEntity;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽象树形结构服务类
 *
 * @author zhouhao
 * @see TreeSortSupportEntity
 * @since 3.0
 */
public abstract class AbstractTreeSortService<E extends TreeSortSupportEntity<PK>, PK>
        extends GenericEntityService<E, PK> implements TreeService<E, PK> {

    @Override
    @Transactional(readOnly = true)
    public List<E> selectAllChildNode(PK parentId) {
        assertNotNull(parentId);
        E old = selectByPk(parentId);
        assertNotNull(old);
        return createQuery().where().like$(TreeSupportEntity.path, old.getPath()).noPaging().list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> selectChildNode(PK parentId) {
        assertNotNull(parentId);
        return createQuery().where(TreeSupportEntity.parentId, parentId).noPaging().list();
    }

    //当父节点不存在时,创建parentId
    @SuppressWarnings("unchecked")
    protected PK createParentIdOnExists() {
        if (getPrimaryKeyType() == String.class) {
            return (PK) "-1";
        }
        return null;
    }

    protected void applyPath(E entity) {
        if (!StringUtils.isEmpty(entity.getParentId())) return;
        if (!StringUtils.isEmpty(entity.getPath())) return;

        TreeSortSupportEntity<PK> parent = selectByPk(entity.getParentId());
        if (null == parent) {
            entity.setParentId(createParentIdOnExists());
            entity.setPath(RandomUtil.randomChar(4));
        } else {
            entity.setPath(parent.getPath() + "-" + RandomUtil.randomChar(4));
        }
    }

    @Override
    public PK insert(E entity) {
        if (entity.getId() == null) entity.setId(getIDGenerator().generate());
        applyPath(entity);
        List<E> childrenList = new ArrayList<>();
        TreeSupportEntity.expandTree2List(entity, childrenList, getIDGenerator());
        super.insert(entity);
        childrenList.forEach(this::saveOrUpdateForSingle);
        return entity.getId();
    }

    @Override
    public List<PK> insertBatch(Collection<E> data) {
        return data.stream()
                .map(this::insert)
                .collect(Collectors.toList());
    }

    public int updateBatch(Collection<E> data) {
        assertNotNull(data);
        return data.stream().map(this::updateByPk).reduce(Math::addExact).orElse(0);
    }

    @Override
    public int updateByPk(E entity) {
        assertNotNull(entity);
        List<E> childrenList = new ArrayList<>();
        TreeSupportEntity.expandTree2List(entity, childrenList, getIDGenerator());
        this.saveOrUpdateForSingle(entity);
        childrenList.forEach(this::saveOrUpdateForSingle);
        return childrenList.size() + 1;
    }

    public PK saveOrUpdateForSingle(E entity) {
        assertNotNull(entity);
        PK id = entity.getId();
        applyPath(entity);
        if (null == id || this.selectByPk(id) == null) {
            if (null == id)
                entity.setId(getIDGenerator().generate());
            return super.insert(entity);
        }
        super.updateByPk(entity);
        return id;
    }

    @Override
    public int deleteByPk(PK id) {
        E old = selectByPk(id);
        assertNotNull(old);
        return DefaultDSLDeleteService.createDelete(getDao())
                // where path like 'path%'
                .where().like$(TreeSupportEntity.path, old.getPath())
                .exec();
    }
}
