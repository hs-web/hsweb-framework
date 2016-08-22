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

package org.hsweb.web.controller.monitor;

import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.cache.monitor.MonitorCache;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 缓存监控控制器，用于管理缓存，监控等操作
 */
@RestController
@RequestMapping("/monitor")
@Authorize(module = "monitor-cache")
@AccessLogger("缓存监控")
public class CacheMonitorController {

    @Autowired(required = false)
    private Map<String, CacheManager> cacheManagerMap = new LinkedHashMap<>();

    @RequestMapping(value = "/cache/managers", method = RequestMethod.GET)
    @AccessLogger("获取管理器")
    public ResponseMessage getManagerList() {
        return ResponseMessage.ok(cacheManagerMap.keySet());
    }

    @RequestMapping(value = "/caches", method = RequestMethod.GET)
    @AccessLogger("获取缓存信息")
    public ResponseMessage getCacheTreeList() {
        List<Map<String, Object>> managers = new ArrayList<>();
        cacheManagerMap.entrySet().forEach(entry -> {
            Map<String, Object> manager = new LinkedHashMap<>();
            manager.put("name", entry.getKey());
            manager.put("size", getTimes(entry.getValue(), MonitorCache::size));
            manager.put("totalTimes", getTimes(entry.getValue(), MonitorCache::getTotalTimes));
            manager.put("hitTimes", getTimes(entry.getValue(), MonitorCache::getHitTimes));
            manager.put("putTimes", getTimes(entry.getValue(), MonitorCache::getPutTimes));
            List<Map<String, Object>> caches = new LinkedList<>();
            manager.put("caches", caches);
            entry.getValue().getCacheNames().forEach(cacheName -> {
                Map<String, Object> cacheData = new LinkedHashMap<>();
                cacheData.put("name", cacheName);
                Cache cache = entry.getValue().getCache(cacheName);
                if (cache instanceof MonitorCache) {
                    MonitorCache monitorCache = ((MonitorCache) cache);
                    cacheData.put("size", monitorCache.size());
                    cacheData.put("totalTimes", monitorCache.getTotalTimes());
                    cacheData.put("hitTimes", monitorCache.getHitTimes());
                    cacheData.put("putTimes", monitorCache.getPutTimes());
                }
                caches.add(cacheData);
            });
            managers.add(manager);
        });
        return ResponseMessage.ok(managers).onlyData();
    }


    @RequestMapping(value = "/cache/{name:.+}", method = RequestMethod.GET)
    @AccessLogger("获取所有名称")
    public ResponseMessage getNameList(@PathVariable("name") String name) {
        CacheManager cacheManager = cacheManagerMap.get(name);
        if (cacheManager != null) {
            return ResponseMessage.ok(cacheManager.getCacheNames());
        }
        throw new NotFoundException("缓存不存在");
    }

    @AccessLogger("获取值")
    @RequestMapping(value = "/cache/{managerName}/{cacheName:.+}/{key:.+}", method = RequestMethod.GET)
    public ResponseMessage getValue(@PathVariable("managerName") String managerName,
                                    @PathVariable("cacheName") String cacheName,
                                    @PathVariable("key") String key) {
        Cache.ValueWrapper val = getCache(managerName, cacheName).get(key);
        if (val != null) return ResponseMessage.ok(val.get());
        throw new NotFoundException("值不存在");
    }

    @RequestMapping(value = "/cache/{managerName}/{cacheName}/{key:.+}", method = RequestMethod.DELETE)
    @Authorize(action = "D")
    @AccessLogger("删除指定key的值")
    public ResponseMessage getEvict(@PathVariable("managerName") String managerName,
                                    @PathVariable("cacheName") String cacheName,
                                    @PathVariable("key") String key) {
        getCache(managerName, cacheName).evict(key);
        return ResponseMessage.ok();
    }

    @RequestMapping(value = "/cache/{managerName}/{cacheName:.+}", method = RequestMethod.DELETE)
    @Authorize(action = "D")
    @AccessLogger("清空")
    public ResponseMessage clearEvict(@PathVariable("managerName") String managerName,
                                      @PathVariable("cacheName") String cacheName) {
        getCache(managerName, cacheName).clear();
        return ResponseMessage.ok();
    }

    @RequestMapping(value = "/cache/{managerName}/{cacheName:.+}", method = RequestMethod.GET)
    @AccessLogger("获取键列表")
    public ResponseMessage getKeySet(@PathVariable("managerName") String managerName,
                                     @PathVariable("cacheName") String cacheName) {
        return ResponseMessage.ok(getMonitorCache(managerName, cacheName).keySet());
    }

    @AccessLogger("获取缓存命中次数")
    @RequestMapping(value = "/cache-hits/{managerName}/{cacheName:.+}", method = RequestMethod.GET)
    public ResponseMessage getHitTimes(@PathVariable("managerName") String managerName,
                                       @PathVariable("cacheName") String cacheName) {
        return ResponseMessage.ok(getMonitorCache(managerName, cacheName).getHitTimes());
    }

    @AccessLogger("获取缓存更新次数")
    @RequestMapping(value = "/cache-puts/{managerName}/{cacheName:.+}", method = RequestMethod.GET)
    public ResponseMessage getPutTimes(@PathVariable("managerName") String managerName,
                                       @PathVariable("cacheName") String cacheName) {
        return ResponseMessage.ok(getMonitorCache(managerName, cacheName).getPutTimes());
    }

    @AccessLogger("获取缓存数量")
    @RequestMapping(value = "/cache-size/{managerName}/{cacheName:.+}", method = RequestMethod.GET)
    public ResponseMessage getSize(@PathVariable("managerName") String managerName,
                                   @PathVariable("cacheName") String cacheName) {
        return ResponseMessage.ok(getMonitorCache(managerName, cacheName).size());
    }

    @AccessLogger("获取缓存获取次数")
    @RequestMapping(value = "/cache-total/{managerName}/{cacheName:.+}", method = RequestMethod.GET)
    public ResponseMessage getTotalTimes(@PathVariable("managerName") String managerName,
                                         @PathVariable("cacheName") String cacheName) {
        return ResponseMessage.ok(getMonitorCache(managerName, cacheName).getTotalTimes());
    }

    @AccessLogger("获取缓存命中总次数")
    @RequestMapping(value = "/cache-hits", method = RequestMethod.GET)
    public ResponseMessage getHitTimes() {
        return ResponseMessage.ok(getTimes(MonitorCache::getHitTimes));
    }

    @AccessLogger("获取缓存更新次数")
    @RequestMapping(value = "/cache-puts", method = RequestMethod.GET)
    public ResponseMessage getPutTimes() {
        return ResponseMessage.ok(getTimes(MonitorCache::getPutTimes));
    }

    @AccessLogger("获取缓存获取次数")
    @RequestMapping(value = "/cache-total", method = RequestMethod.GET)
    public ResponseMessage getTotalTimes() {
        return ResponseMessage.ok(getTimes(MonitorCache::getTotalTimes));
    }

    @AccessLogger("获取缓存数量")
    @RequestMapping(value = "/cache-size", method = RequestMethod.GET)
    public ResponseMessage getSize() {
        return ResponseMessage.ok(getTimes(MonitorCache::size));
    }

    protected long getTimes(TimesGetter getter) {
        long times = cacheManagerMap.values().stream()
                .mapToLong(cacheManager -> getTimes(cacheManager, getter))
                .reduce((i1, i2) -> i1 + i2).orElseGet(() -> 0);
        return times;
    }

    protected long getTimes(CacheManager cacheManager, TimesGetter getter) {
        long times = cacheManager.getCacheNames().parallelStream()
                .map(name -> cacheManager.getCache(name))
                .filter(cache -> cache instanceof MonitorCache)
                .map(cache -> (MonitorCache) cache)
                .mapToLong(getter::get)
                .reduce((i1, i2) -> i1 + i2).orElseGet(() -> 0);
        return times;
    }

    protected MonitorCache getMonitorCache(String managerName, String cacheName) {
        Cache cache = getCache(managerName, cacheName);
        if (cache instanceof MonitorCache) {
            return ((MonitorCache) cache);
        }
        throw new NotFoundException("缓存不支持监控");
    }

    protected Cache getCache(String managerName, String cacheName) {
        CacheManager cacheManager = cacheManagerMap.get(managerName);
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                return cache;
            }
        }
        throw new NotFoundException("缓存不存在");
    }

    interface TimesGetter {
        long get(MonitorCache cache);
    }

}
