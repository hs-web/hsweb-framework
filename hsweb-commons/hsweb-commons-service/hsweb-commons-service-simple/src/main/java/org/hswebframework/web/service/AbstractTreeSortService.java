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

import org.hswebframework.web.commons.entity.TreeSortSupportEntity;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public abstract class AbstractTreeSortService<E extends TreeSortSupportEntity<PK>, PK>
        extends GenericEntityService<E, PK> implements TreeService<E, PK> {

    @Override
    @Transactional(readOnly = true)
    public List<E> selectAllChildNode(PK parentId) {
        assertNotNull(parentId);
        E old = selectByPk(parentId);
        assertNotNull(old);
        return createQuery().where().like$(TreeSupportEntity.treeCode, old.getTreeCode()).noPaging().list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<E> selectChildNode(PK parentId) {
        assertNotNull(parentId);
        return createQuery().where(TreeSupportEntity.parentId, parentId).noPaging().list();
    }

    @Override
    public PK insert(E entity) {
        entity.setId(getIDGenerator().generate());
        List<E> childrenList = new ArrayList<>();
        TreeSupportEntity.expandTree2List(entity, childrenList, getIDGenerator());
        super.insert(entity);
        childrenList.forEach(this::saveOrUpdateForSingle);
        return entity.getId();
    }

    public int updateBatch(Collection<E> data) {
        Assert.notNull(data);
        return data.stream().map(this::updateByPk).reduce(Math::addExact).orElse(0);
    }

    @Override
    public int updateByPk(E entity) {
        Assert.notNull(entity);
        List<E> childrenList = new ArrayList<>();
        TreeSupportEntity.expandTree2List(entity, childrenList, getIDGenerator());
        return this.saveOrUpdateForSingle(entity) +
                childrenList.stream()
                        .map(this::saveOrUpdateForSingle)
                        .reduce(Math::addExact)
                        .orElse(0);
    }

    public int saveOrUpdateForSingle(E entity) {
        Assert.notNull(entity);
        PK id = entity.getId();
        if (null == id || this.selectByPk(id) == null) {
            if (null == id)
                entity.setId(getIDGenerator().generate());
            super.insert(entity);
            return 1;
        }
        return super.updateByPk(entity);
    }

    @Override
    public int deleteByPk(PK id) {
        E old = selectByPk(id);
        assertNotNull(old);
        return DefaultDSLDeleteService.createDelete(getDao())
                // where tree_code like 'treeCode%'
                .where().like$(TreeSupportEntity.treeCode, old.getTreeCode())
                .exec();
    }
}
