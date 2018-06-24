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

import org.hswebframework.web.commons.entity.TreeSupportEntity;

import java.util.Collection;
import java.util.List;

/**
 * 树结构实体服务,提供对树结果实体的常用操作
 *
 * @author zhouhao
 * @since 3.0
 */
public interface TreeService<E extends TreeSupportEntity, PK> extends Service {

    /**
     * 查询所有父节点
     * @param childId 子节点id
     * @return 父节点集合
     */
    List<E> selectParentNode(PK childId);

    /**
     * 根据父节点id获取子节点数据
     *
     * @param parentId 父节点ID
     * @return 子节点数据
     */
    List<E> selectChildNode(PK parentId);

    /**
     * 根据父节点id,获取所有子节点的数据,包含字节点的字节点
     *
     * @param parentId 父节点ID
     * @return 所有子节点的数据
     */
    List<E> selectAllChildNode(PK parentId);

    /**
     * 批量修改数据,如果集合中的数据不存在,则将会进行新增
     *
     * @param data 数据集合
     * @return 修改的数量
     */
    int updateBatch(Collection<E> data);

    /**
     * 批量添加数据
     *
     * @param data 数据集合
     * @return 被添加数据集合的主键
     */
    List<PK> insertBatch(Collection<E> data);
}
