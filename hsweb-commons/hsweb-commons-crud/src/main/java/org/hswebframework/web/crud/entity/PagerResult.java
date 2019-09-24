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

package org.hswebframework.web.crud.entity;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PagerResult<E> {
    private static final long serialVersionUID = -6171751136953308027L;

    public static <E> PagerResult<E> empty() {
        return new PagerResult<>(0, new ArrayList<>());
    }

    public static <E> PagerResult<E> of(int total, List<E> list) {
        return new PagerResult<>(total, list);
    }

    public static <E> PagerResult<E> of(int total, List<E> list, QueryParamEntity entity) {
        PagerResult<E> pagerResult = new PagerResult<>(total, list);
        pagerResult.setPageIndex(entity.getThinkPageIndex());
        pagerResult.setPageSize(entity.getPageSize());
        return pagerResult;
    }

    private int pageIndex;

    private int pageSize;

    private int total;

    private List<E> data;

    public PagerResult() {
    }

    public PagerResult(int total, List<E> data) {
        this.total = total;
        this.data = data;
    }

}
