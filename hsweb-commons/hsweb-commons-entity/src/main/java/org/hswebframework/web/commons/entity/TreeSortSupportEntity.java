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

import org.hswebframework.web.Describe;

/**
 * 支持树形结构，排序的实体类，要使用树形结构，排序功能的实体类直接继承该类
 */
public abstract class TreeSortSupportEntity extends SimpleGenericEntity<String> implements TreeSupport, SortSupport {

    /**
     * 父级类别
     */
    @Describe("父级类别ID")
    private String parentId;

    /**
     * 树结构编码,用于快速查找, 每一层由4位字符组成,用-分割
     * 如第一层:0001 第二层:0001-0001 第三层:0001-0001-0001
     */
    @Describe("树识别码")
    private String treeCode;

    /**
     * 排序索引
     */
    @Describe("排序索引")
    private long sortIndex;

    @Override
    public String getTreeCode() {
        return treeCode;
    }

    @Override
    public void setTreeCode(String treeCode) {
        this.treeCode = treeCode;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    @Override
    public long getSortIndex() {
        return sortIndex;
    }

    @Override
    public TreeSortSupportEntity setSortIndex(long sortIndex) {
        this.sortIndex = sortIndex;
        return this;
    }

    public interface Property {
        /**
         * 父级类别
         */
        String parentId = "parentId";

        /**
         * 树结构编码,用于快速查找, 每一层由4位字符组成,用-分割
         * 如第一层:0001 第二层:0001-0001 第三层:0001-0001-0001
         */
        String treeCode = "treeCode";

        /**
         * 排序索引
         */
        String sortIndex = "sortIndex";
    }
}