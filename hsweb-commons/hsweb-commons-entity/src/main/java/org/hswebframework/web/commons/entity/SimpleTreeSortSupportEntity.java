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

package org.hswebframework.web.commons.entity;


import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;

import javax.persistence.Column;

/**
 * 支持树形结构，排序的实体类，要使用树形结构，排序功能的实体类直接继承该类
 */
@Getter
@Setter
public abstract class SimpleTreeSortSupportEntity<PK> extends SimpleGenericEntity<PK>
        implements TreeSortSupportEntity<PK> {
    /**
     * 父级类别
     */
    @Column(name = "parent_id", length = 32)
    @Comment("父级ID")
    private PK parentId;

    /**
     * 树结构编码,用于快速查找, 每一层由4位字符组成,用-分割
     * 如第一层:0001 第二层:0001-0001 第三层:0001-0001-0001
     */
    @Column(name = "path", length = 128)
    @Comment("树路径")
    private String path;

    /**
     * 排序索引
     */
    @Column(name = "sort_index", precision = 32)
    @Comment("排序序号")
    private Long sortIndex;

    @Column(name = "_level", precision = 32)
    @Comment("树层级")
    private Integer level;


}