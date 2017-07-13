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


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Collections;
import java.util.List;

@ApiModel(description = "分页结果")
public class PagerResult<E> implements Entity {
    private static final long serialVersionUID = -6171751136953308027L;

    public static <E> PagerResult<E> empty(){
        return new PagerResult<>(0, Collections.emptyList());
    }

    public static <E> PagerResult<E> of(int total,List<E> list){
        return new PagerResult<>(total,list);
    }
    private int total;

    private List<E> data;

    public PagerResult() {
    }

    public PagerResult(int total, List<E> data) {
        this.total = total;
        this.data = data;
    }

    @ApiModelProperty("数据总数量")
    public int getTotal() {
        return total;
    }

    public PagerResult<E> setTotal(int total) {
        this.total = total;
        return this;
    }

    @ApiModelProperty("查询结果")
    public List<E> getData() {
        return data;
    }

    public PagerResult<E> setData(List<E> data) {
        this.data = data;
        return this;
    }

}
