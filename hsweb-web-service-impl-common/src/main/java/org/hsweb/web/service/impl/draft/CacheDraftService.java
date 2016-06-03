package org.hsweb.web.service.impl.draft;

import org.hsweb.web.bean.po.draft.Draft;
import org.hsweb.web.service.draft.DraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhouhao on 16-6-3.
 */
@Service
public class CacheDraftService implements DraftService {
    @Autowired(required = false)
    private CacheManager cacheManager;

    private String cacheKey = "draft:";

    @PostConstruct
    public void init() {
        if (cacheManager == null) {
            cacheManager = new ConcurrentMapCacheManager();
        }
    }

    @Override
    public String createDraft(String key, Draft draft) {
        Cache cache = cacheManager.getCache(cacheKey + draft.getCreatorId());
        Cache.ValueWrapper wrapper = cache.get(key);
        Map<String, Draft> drafts;
        if (wrapper == null) {
            drafts = new HashMap<>();
        } else {
            drafts = ((Map) wrapper.get());
        }
        drafts.put(draft.getId(), draft);
        cache.put(key, drafts);
        return draft.getId();
    }

    @Override
    public List<Draft> getAllDraftByKey(String key, String userId) {
        Cache cache = cacheManager.getCache(cacheKey + userId);
        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper != null) {
            return (List) ((Map) wrapper.get()).values();
        }
        return new ArrayList<>();
    }

    @Override
    public boolean removeDraft(String key, String id, String userId) {
        Cache cache = cacheManager.getCache(cacheKey + userId);
        Cache.ValueWrapper wrapper = cache.get(key);
        if (wrapper != null) {
            Map<String, Draft> drafts = ((Map) wrapper.get());
            drafts.remove(id);
            cache.put(key, drafts);
            return true;
        }
        return false;
    }

    @Override
    public boolean removeDraft(String key, String userId) {
        Cache cache = cacheManager.getCache(cacheKey + userId);
        cache.evict(key);
        return true;
    }
}
