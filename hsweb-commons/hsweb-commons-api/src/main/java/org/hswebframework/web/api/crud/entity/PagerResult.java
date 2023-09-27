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


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.core.param.QueryParam;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页查询结果,用于在分页查询时,定义查询结果.如果需要拓展此类,例如自定义json序列化,请使用spi方式定义拓展实现类型:
 * <pre>{@code
 * ---resources
 * -----|--META-INF
 * -----|----services
 * -----|------org.hswebframework.web.api.crud.entity.PagerResult
 * }</pre>
 * <p>
 *
 * @param <E> 结果类型
 * @author zhouhao
 * @since 4.0.0
 */
@Getter
@Setter
public class PagerResult<E> implements Serializable {
    private static final long serialVersionUID = -6171751136953308027L;

    /**
     * 创建一个空结果
     *
     * @param <E> 结果类型
     * @return PagerResult
     */
    public static <E> PagerResult<E> empty() {
        return of(0, new ArrayList<>());
    }

    /**
     * 创建一个分页结果
     *
     * @param total 总数据量
     * @param list  当前页数据列表
     * @param <E>   结果类型
     * @return PagerResult
     */
    @SuppressWarnings("all")
    public static <E> PagerResult<E> of(int total, List<E> list) {
        PagerResult<E> result;
        result = EntityFactoryHolder.newInstance(PagerResult.class, PagerResult::new);
        result.setTotal(total);
        result.setData(list);
        return result;
    }

    /**
     * 创建一个分页结果,并将查询参数中的分页索引等信息填充到分页结果中
     *
     * @param total  总数据量
     * @param list   当前页数据列表
     * @param entity 查询参数
     * @param <E>    结果类型
     * @return PagerResult
     */
    public static <E> PagerResult<E> of(int total, List<E> list, QueryParam entity) {
        PagerResult<E> pagerResult = of(total, list);
        pagerResult.setPageIndex(entity.getThinkPageIndex());
        pagerResult.setPageSize(entity.getPageSize());
        return pagerResult;
    }

    @Schema(description = "页码")
    private int pageIndex;

    @Schema(description = "每页数据量")
    private int pageSize;

    @Schema(description = "数据总量")
    private int total;

    @Schema(description = "数据列表")
    private List<E> data;

    public PagerResult() {
    }

    public PagerResult(int total, List<E> data) {
        this.total = total;
        this.data = data;
    }
}
