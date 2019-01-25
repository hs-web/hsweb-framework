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


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;

import java.util.ArrayList;
import java.util.List;

@ApiModel(description = "分页结果")
@Getter
@Setter
public class PagerResult<E> implements Entity {
    private static final long serialVersionUID = -6171751136953308027L;

    public static <E> PagerResult<E> empty() {
        return new PagerResult<>(0, new ArrayList<>());
    }

    public static <E> PagerResult<E> of(int total, List<E> list) {
        return new PagerResult<>(total, list);
    }

    public static <E> PagerResult<E> of(int total, List<E> list, QueryParamEntity entity) {
        PagerResult pagerResult = new PagerResult<>(total, list);
        pagerResult.setPageIndex(entity.getThinkPageIndex());
        pagerResult.setPageSize(entity.getPageSize());
        return pagerResult;
    }


    @ApiModelProperty("当前页码")
    private int pageIndex;

    @ApiModelProperty("每页数据数量")
    private int pageSize;

    @ApiModelProperty("数据总数量")
    private int total;

    @ApiModelProperty("查询结果")
    private List<E> data;

    public PagerResult() {
    }

    public PagerResult(int total, List<E> data) {
        this.total = total;
        this.data = data;
    }

}
