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


import java.util.List;

public class PagerResult<B> implements Entity {
    private static final long serialVersionUID = -6171751136953308027L;
    private int total;

    private List<B> data;

    public PagerResult() {
    }

    public PagerResult(int total, List<B> data) {
        this.total = total;
        this.data = data;
    }

    public int getTotal() {
        return total;
    }

    public PagerResult<B> setTotal(int total) {
        this.total = total;
        return this;
    }

    public List<B> getData() {
        return data;
    }

    public PagerResult<B> setData(List<B> data) {
        this.data = data;
        return this;
    }

}
