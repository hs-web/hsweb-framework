package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.service.authorization.UserSettingService;
import org.hswebframework.web.dao.authorization.UserSettingDao;
import org.hswebframework.web.entity.authorization.UserSettingEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.EnableCacheGenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author zhouhao
 * @since 3.0
 */
@Service
@CacheConfig(cacheNames = "user-setting")
public class SimpleUserSettingService extends EnableCacheGenericEntityService<UserSettingEntity, String>
        implements UserSettingService {

    @Autowired
    private UserSettingDao userSettingDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public UserSettingDao getDao() {
        return userSettingDao;
    }

    @Override
    protected boolean dataExisted(UserSettingEntity entity) {
        UserSettingEntity old = createQuery()
                .where(entity::getUserId)
                .and(entity::getKey)
                .and(entity::getSettingId)
                .single();
        if (old != null) {
            entity.setId(old.getId());
            return true;
        }
        return false;
    }

    @Override
    @Cacheable(key = "'user:'+#userId+'.'+#key")
    public List<UserSettingEntity> selectByUser(String userId, String key) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(key);

        return createQuery().where("userId", userId).and("key", key).listNoPaging();
    }

    @Override
    @Cacheable(key = "'user:'+#userId+'.'+#key+'.'+#settingId")
    public UserSettingEntity selectByUser(String userId, String key, String settingId) {
        Objects.requireNonNull(userId);
        Objects.requireNonNull(key);
        Objects.requireNonNull(settingId);
        return createQuery().where("userId", userId).and("key", key).and("settingId", settingId).single();
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'user:'+#entity.userId+'.'+#entity.key+'.'+#entity.settingId"),
                    @CacheEvict(key = "'user:'+#entity.userId+'.'+#entity.key")
            }
    )
    public String insert(UserSettingEntity entity) {
        entity.setCreateTime(new Date());
        entity.setUpdateTime(new Date());
        return super.insert(entity);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'user:'+#entity.userId+'.'+#entity.key+'.'+#entity.settingId"),
                    @CacheEvict(key = "'user:'+#entity.userId+'.'+#entity.key")
            }
    )
    public String saveOrUpdate(UserSettingEntity entity) {
        return super.saveOrUpdate(entity);
    }

    @Override
    @Caching(
            evict = {
                    @CacheEvict(key = "'user:'+#entity.userId+'.'+#entity.key+'.'+#entity.settingId"),
                    @CacheEvict(key = "'user:'+#entity.userId+'.'+#entity.key")
            }
    )
    public int updateByPk(String id, UserSettingEntity entity) {
        entity.setUpdateTime(new Date());
        return super.updateByPk(id, entity);
    }
}
