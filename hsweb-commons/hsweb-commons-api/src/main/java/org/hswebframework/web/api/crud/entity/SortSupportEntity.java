/*
 *
 *  * Copyright 2020 http://www.hswebframework.org
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

package org.hswebframework.web.api.crud.entity;

import jakarta.annotation.Nonnull;

/**
 * 支持排序的实体
 *
 * @author zhouhao
 * @since 4.0.0
 */
public interface SortSupportEntity extends Comparable<SortSupportEntity>, Entity {

    /**
     * @return 排序序号
     */
    Long getSortIndex();

    /**
     * 设置排序序号
     *
     * @param sortIndex 排序序号
     */
    void setSortIndex(Long sortIndex);

    @Override
    default int compareTo(@Nonnull SortSupportEntity support) {
        return Long.compare(getSortIndex() == null ? 0 : getSortIndex(), support.getSortIndex() == null ? 0 : support.getSortIndex());
    }
}
