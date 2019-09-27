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
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.dao.dynamic.QueryByEntityDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DefaultQueryByEntityService<E>
        extends QueryByEntityService<E> {

    SyncRepository<E, ?> getDao();

    /**
     * 分页进行查询数据，查询条件同 {@link DefaultQueryByEntityService#select}
     *
     * @param entity 查询参数
     * @return 分页查询结果
     * @see QueryParamEntity
     * @see QueryParamEntity#newQuery()
     */
    @Override
    default PagerResult<E> selectPager(QueryParamEntity entity) {
        PagerResult<E> pagerResult = new PagerResult<>();

        //不分页,不进行count
        if (!entity.isPaging()) {
            pagerResult.setData(getDao().createQuery().setParam(entity).fetch());
            pagerResult.setTotal(pagerResult.getData().size());
            pagerResult.setPageIndex(entity.getThinkPageIndex());
            pagerResult.setPageSize(pagerResult.getData().size());
            return pagerResult;
        }
        int total = getDao().createQuery().setParam(entity).count();
        pagerResult.setTotal(total);

        entity.rePaging(total);
        pagerResult.setPageSize(entity.getPageSize());
        pagerResult.setPageIndex(entity.getThinkPageIndex());

        if (total == 0) {
            pagerResult.setData(new java.util.ArrayList<>());
        } else {
            pagerResult.setData(select(entity));
        }
        return pagerResult;
    }

    /**
     * 根据查询参数进行查询，参数可使用 {@link Query}进行构建
     *
     * @param param 查询参数
     * @return 查询结果
     * @see QueryParamEntity
     * @see QueryParamEntity#newQuery()
     */
    @Override
    @Transactional(readOnly = true)
    default List<E> select(QueryParamEntity param) {
        if (param == null) {
            param = QueryParamEntity.empty();
        }
        return getDao().createQuery().setParam(param).fetch();
    }


    /**
     * 查询记录总数，用于分页等操作。查询条件同 {@link DefaultQueryByEntityService#select}
     *
     * @param param 查询参数
     * @return 查询结果，实现mapper中的sql应指定默认值，否则可能抛出异常
     * @see QueryParamEntity
     * @see QueryParamEntity#newQuery()
     */
    @Override
    @Transactional(readOnly = true)
    default int count(QueryParamEntity param) {
        if (param == null) {
            param = QueryParamEntity.empty();
        }
        return getDao().createQuery().setParam(param).count();
    }

    /**
     * 查询只返回单个结果,如果有多个结果,只返回第一个
     *
     * @param param 查询条件
     * @return 单个查询结果
     * @see QueryParamEntity
     * @see QueryParamEntity#newQuery()
     */
    @Override
    @Transactional(readOnly = true)
    default E selectSingle(QueryParamEntity param) {

        return getDao().createQuery().setParam(param).fetchOne().orElse(null);
    }

}
