package org.hsweb.web.mybatis.plgins.pager;

import org.hsweb.web.core.utils.ThreadLocalUtils;

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
