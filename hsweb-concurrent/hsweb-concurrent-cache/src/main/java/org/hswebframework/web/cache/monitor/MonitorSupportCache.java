package org.hswebframework.web.cache.monitor;

import org.springframework.cache.Cache;

import java.util.Set;

/**
 * 支持监控的缓存
 *
 * @author zhouhao
 */
public interface MonitorSupportCache extends Cache {
    long getTotalTimes();

    long getHitTimes();

    long getUpdateTimes();

    long size();

    Set<Object> keySet();
}
