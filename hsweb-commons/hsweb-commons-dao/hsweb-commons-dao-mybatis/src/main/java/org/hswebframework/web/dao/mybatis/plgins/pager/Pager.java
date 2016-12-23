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

package org.hswebframework.web.dao.mybatis.plgins.pager;


import org.hswebframework.web.ThreadLocalUtils;

/**
 * 分页插件,通过此接口进行分页操作
 *
 * @author zhouhao
 * @see PagerInterceptor
 */
public interface Pager {
    int pageIndex();

    int pageSize();

    String threadLocalKey = "nowPager";

    static Pager getAndReset() {
        try {
            return get();
        } finally {
            reset();
        }
    }

    static Pager get() {
        return ThreadLocalUtils.get(threadLocalKey);
    }

    static void reset() {
        ThreadLocalUtils.remove(threadLocalKey);
    }

    static void doPaging(int pageIndex, int pageSize) {
        ThreadLocalUtils.put(threadLocalKey, new Pager() {
            @Override
            public int pageIndex() {
                return pageIndex;
            }

            @Override
            public int pageSize() {
                return pageSize;
            }
        });
    }

    static void doPaging(int pageIndex, int pageSize, int total) {
        doPaging(pageIndex, pageSize);
        rePaging(total);
    }

    static void rePaging(int total) {
        Pager pager = get();
        int pageIndex = 0;
        if (pager != null) {
            // 当前页没有数据后跳转到最后一页
            if (pager.pageIndex() != 0 && (pager.pageIndex() * pager.pageSize()) >= total) {
                int tmp = total / pager.pageSize();
                pageIndex = total % pager.pageSize() == 0 ? tmp - 1 : tmp;
            }
            doPaging(pageIndex, pager.pageSize());
        }
    }
}
