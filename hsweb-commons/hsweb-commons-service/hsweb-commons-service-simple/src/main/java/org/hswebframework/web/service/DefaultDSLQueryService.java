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

import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.ezorm.rdb.mapping.SyncQuery;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.dao.dynamic.QueryByEntityDao;

import java.util.List;

public interface DefaultDSLQueryService<E, PK>
        extends DefaultQueryByEntityService<E>, QueryService<E, PK> {

    @Override
    default List<E> select() {
        return createQuery().fetch();
    }

    @Override
    default int count() {
        return createQuery().count();
    }

    /**
     * 创建本服务的dsl查询操作对象
     * 可通过返回的Query对象进行dsl方式操作如:<br>
     * <code>
     * createQuery().where("id",1).fetch();
     * </code>
     *
     * @return {@link Query}
     * @see Query
     * @see org.hswebframework.ezorm.core.Conditional
     * @since 3.1
     */
    default SyncQuery<E> createQuery() {

        return getDao().createQuery();
    }

    /**
     * 指定一个dao映射接口,接口需继承{@link QueryByEntityDao}创建dsl数据查询对象<br>
     * 可通过返回的Query对象进行dsl方式操作如:<br>
     * <code>
     * createQuery(userMapper).where("id",1).single();
     * </code>
     *
     * @param dao dao接口
     * @return {@link Query}
     * @see Query
     * @see org.hswebframework.ezorm.core.Conditional
     * @since 3.1
     */
    static <E> SyncQuery<E> createQuery(SyncRepository<E, ?> dao) {

        return dao.createQuery();
    }
}
