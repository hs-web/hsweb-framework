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

package org.hswebframework.web.commons.entity;


/**
 * 支持树形结构，排序的实体类，要使用树形结构，排序功能的实体类直接继承该类
 */
public abstract class SimpleTreeSortSupportEntity<PK> extends SimpleGenericEntity<PK>
        implements TreeSortSupportEntity<PK> {
    /**
     * 父级类别
     */
    private PK parentId;

    /**
     * 树结构编码,用于快速查找, 每一层由4位字符组成,用-分割
     * 如第一层:0001 第二层:0001-0001 第三层:0001-0001-0001
     */
    private String path;

    /**
     * 排序索引
     */
    private Long sortIndex;

    private Integer level;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public PK getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(PK parentId) {
        this.parentId = parentId;
    }

    @Override
    public Long getSortIndex() {
        return sortIndex;
    }

    @Override
    public Integer getLevel() {
        return level;
    }

    @Override
    public void setLevel(Integer level) {
        this.level = level;
    }

    @Override
    public void setSortIndex(Long sortIndex) {
        this.sortIndex = sortIndex;
    }

}