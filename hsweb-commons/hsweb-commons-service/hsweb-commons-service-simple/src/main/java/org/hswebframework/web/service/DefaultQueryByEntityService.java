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

package org.hswebframework.web.service;

import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.dao.dynamic.QueryByEntityDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

public interface DefaultQueryByEntityService<E>
        extends QueryByEntityService<E> {

    QueryByEntityDao<E> getDao();

    /**
     * 分页进行查询数据，查询条件同 {@link DefaultQueryByEntityService#select}
     *
     * @param param 查询参数
     * @return 分页查询结果
     */
    @Override
    default PagerResult<E> selectPager(Entity param) {
        PagerResult<E> pagerResult = new PagerResult<>();
        if (param instanceof QueryParamEntity) {
            QueryParamEntity entity = ((QueryParamEntity) param);
            //不分页,不进行count
            if (!entity.isPaging()) {
                pagerResult.setData(getDao().query(param));
                pagerResult.setTotal(pagerResult.getData().size());
                return pagerResult;
            }
        }
        int total = getDao().count(param);
        pagerResult.setTotal(total);
        if (total == 0) {
            pagerResult.setData(new java.util.ArrayList<>());
        } else {
            //根据实际记录数量重新指定分页参数
            if (param instanceof QueryParamEntity) {
                ((QueryParamEntity) param).rePaging(total);
            }
            pagerResult.setData(getDao().query(param));
        }
        return pagerResult;
    }

    /**
     * 根据查询参数进行查询，参数可使用 {@link Query}进行构建
     *
     * @param param 查询参数
     * @return 查询结果
     * @see QueryParamEntity
     */
    @Override
    @Transactional(readOnly = true)
    default List<E> select(Entity param) {
        if (param == null) {
            param = QueryParamEntity.empty();
        }
        return getDao().query(param);
    }


    /**
     * 查询记录总数，用于分页等操作。查询条件同 {@link DefaultQueryByEntityService#select}
     *
     * @param param 查询参数
     * @return 查询结果，实现mapper中的sql应指定默认值，否则可能抛出异常
     */
    @Override
    @Transactional(readOnly = true)
    default int count(Entity param) {
        if (param == null) {
            param = QueryParamEntity.empty();
        }
        return getDao().count(param);
    }

    /**
     * 查询只返回单个结果,如果有多个结果,只返回第一个
     *
     * @param param 查询条件
     * @return 单个查询结果
     */
    @Override
    @Transactional(readOnly = true)
    default E selectSingle(Entity param) {
        if (param instanceof QueryParamEntity) {
            ((QueryParamEntity) param).doPaging(0, 1);
        }
        List<E> list = this.select(param);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

}
