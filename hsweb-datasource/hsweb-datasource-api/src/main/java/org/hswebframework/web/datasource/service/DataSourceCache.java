package org.hswebframework.web.datasource.service;

import org.hswebframework.web.datasource.DynamicDataSource;
import org.hswebframework.web.datasource.exception.DataSourceClosedException;

import java.util.concurrent.CountDownLatch;

/**
 * 数据源缓存
 *
 * @author zhouhao
 */
public class DataSourceCache {
    private long hash;

    private volatile boolean closed;

    private DynamicDataSource dataSource;

    private volatile CountDownLatch initLatch;

    public long getHash() {
        return hash;
    }

    public DynamicDataSource getDataSource() {
        if (initLatch != null) {
            try {
                //等待初始化完成
                initLatch.await();
            } catch (InterruptedException ignored) {
            } finally {
                initLatch = null;
            }
        }
        if (closed) {
            throw new DataSourceClosedException(dataSource.getId());
        }
        return dataSource;
    }

    public DataSourceCache(long hash, DynamicDataSource dataSource, CountDownLatch initLatch) {
        this.hash = hash;
        this.dataSource = dataSource;
        this.initLatch = initLatch;
    }

    public boolean isClosed() {
        return closed;
    }


    public void closeDataSource() {
        closed = true;
    }
}
