/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.core.cache.monitor;

import org.springframework.cache.Cache;

import java.util.Set;

/**
 * 缓存监控功能
 */
public interface MonitorCache extends Cache {

    /**
     * 缓存中所有key
     *
     * @return {@link Set<Object>} key集合
     */
    Set<Object> keySet();

    /**
     * @return 缓存数量
     */
    int size();

    /**
     * 缓存调用次数
     *
     * @return 调用次数
     */
    long getTotalTimes();

    /**
     * 缓存命中次数
     *
     * @return 命中次数
     */
    long getHitTimes();

    /**
     * 执行缓存更新次数
     *
     * @return 缓存更新次数
     */
    long getPutTimes();
}
