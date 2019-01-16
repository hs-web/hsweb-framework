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

package org.hswebframework.web.dao;

import org.hswebframework.web.dao.dynamic.UpdateByEntityDao;
import org.hswebframework.web.dao.dynamic.DeleteByEntityDao;
import org.hswebframework.web.dao.dynamic.QueryByEntityDao;

/**
 * 通用增删改查DAO接口,定义了增删改查.以及动态条件查询,修改,删除。
 *
 * @param <E> PO类型
 * @param <PK> 主键类型
 * @author zhouhao
 * @see InsertDao
 * @see DeleteDao
 * @see DeleteByEntityDao
 * @see UpdateByEntityDao
 * @see QueryByEntityDao
 * @since 3.0
 */
public interface CrudDao<E, PK> extends
        InsertDao<E>,
        DeleteDao<PK>,
        DeleteByEntityDao,
        UpdateByEntityDao,
        QueryByEntityDao<E> {
}
