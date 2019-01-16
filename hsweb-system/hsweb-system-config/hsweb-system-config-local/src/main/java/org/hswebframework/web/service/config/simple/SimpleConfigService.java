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

package org.hswebframework.web.service.config.simple;

import org.hswebframework.web.dao.config.ConfigDao;
import org.hswebframework.web.entity.config.ConfigContent;
import org.hswebframework.web.entity.config.ConfigEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.EnableCacheAllEvictGenericEntityService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * @author zhouhao
 */
@Service("configService")
@CacheConfig(cacheNames = "hsweb-config")
public class SimpleConfigService extends EnableCacheAllEvictGenericEntityService<ConfigEntity, String>
        implements ConfigService {

    @Autowired
    private ConfigDao configDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public ConfigDao getDao() {
        return configDao;
    }

    protected Optional<ConfigContent> getConfigContent(String configId, String key) {
        ConfigEntity entity = selectByPk(configId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(entity.get(key));

    }

    @Override
    @Cacheable(key = "'id:'+#configId+'.'+#key+'-as-number'")
    public Number getNumber(String configId, String key, Number defaultValue) {

        return getConfigContent(configId, key)
                .map(conf -> conf.getNumber(defaultValue))
                .orElse(defaultValue);
    }

    @Override
    @Cacheable(key = "'id:'+#configId+'.'+#key+'-as-string'")
    public String getString(String configId, String key, String defaultValue) {
        return getConfigContent(configId, key)
                .map(conf -> conf.getValue(defaultValue))
                .map(String::valueOf)
                .orElse(defaultValue);
    }

    @Override
    @Cacheable(key = "'id:'+#configId+'.'+#key+'-as-boolean'")
    public boolean getBoolean(String configId, String key, boolean defaultValue) {
        return getConfigContent(configId, key)
                .map(conf -> conf.getValue(defaultValue))
                .map(val -> Boolean.TRUE.equals(val) || Integer.valueOf(1).equals(val))
                .orElse(defaultValue);
    }
}
