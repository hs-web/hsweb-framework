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

import org.hsweb.ezorm.core.dsl.Query;
import org.hsweb.ezorm.core.param.QueryParam;
import org.hswebframework.web.commons.beans.PagerResult;
import org.hswebframework.web.commons.beans.param.QueryParamBean;
import org.hswebframework.web.dao.dynamic.QueryByBeanDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

public interface DefaultQueryByBeanService<B>
        extends QueryByBeanService<B, QueryParamBean> {
    QueryByBeanDao<B> getDao();

    @Override
    default PagerResult<B> selectPager(QueryParamBean param) {
        PagerResult<B> pagerResult = new PagerResult<>();
        param.setPaging(false);
        int total = getDao().count(param);
        pagerResult.setTotal(total);
        if (total == 0) {
            pagerResult.setData(new ArrayList<>());
        } else {
            //根据实际记录数量重新指定分页参数
            param.rePaging(total);
            pagerResult.setData(getDao().query(param));
        }
        return pagerResult;
    }

    /**
     * 分页进行查询数据，查询条件同 {@link DefaultQueryByBeanService#select}
     *
     * @param param 查询参数
     * @return 分页结果
     * @ 查询异常
     */

    /**
     * 根据查询参数进行查询，参数可使用 {@link Query}进行构建
     *
     * @param param 查询参数
     * @return 查询结果
     * @see QueryParam
     */
    @Override
    @Transactional(readOnly = true)
    default List<B> select(QueryParamBean param) {
        return getDao().query(param);
    }

    /**
     * 查询所有数据
     *
     * @return 所有数据
     */
    @Override
    @Transactional(readOnly = true)
    default List<B> select() {
        return getDao().query(new QueryParamBean());
    }

    /**
     * 查询记录总数，用于分页等操作。查询条件同 {@link DefaultQueryByBeanService#select}
     *
     * @param param 查询参数
     * @return 查询结果，实现mapper中的sql应指定默认值，否则可能抛出异常
     */
    @Override
    @Transactional(readOnly = true)
    default int count(QueryParamBean param) {
        return getDao().count(param);
    }

    /**
     * 查询只返回单个结果
     *
     * @param param 查询条件
     * @return 单个结果
     * @see this#select(QueryParamBean)
     */
    @Override
    @Transactional(readOnly = true)
    default B selectSingle(QueryParamBean param) {
        param.doPaging(0, 1);
        List<B> list = this.select(param);
        if (list.size() == 0) return null;
        else return list.get(0);
    }

}
