/*
 *  Copyright 2019 http://www.hswebframework.org
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.service.authorization.MenuGroupService;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.dao.authorization.MenuGroupDao;
import org.hswebframework.web.entity.authorization.MenuEntity;
import org.hswebframework.web.entity.authorization.MenuGroupBindEntity;
import org.hswebframework.web.entity.authorization.MenuGroupEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.hswebframework.web.service.authorization.MenuGroupBindService;
import org.hswebframework.web.service.authorization.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("menuGroupService")
@CacheConfig(cacheNames = CacheConstants.MENU_CACHE_NAME)
public class SimpleMenuGroupService
        extends AbstractTreeSortService<MenuGroupEntity, String>
        implements MenuGroupService {
    @Autowired
    private MenuGroupDao menuGroupDao;

    @Autowired
    private MenuService menuService;

    @Autowired
    private MenuGroupBindService menuGroupBindService;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public MenuGroupDao getDao() {
        return menuGroupDao;
    }

    @Override
    @Cacheable(key = "'group-id-list:'+(#groupId==null?0:#groupId.hashCode())")
    public List<MenuEntity> getMenuByGroupId(List<String> groupId) {
        List<MenuGroupBindEntity> bindEntities = menuGroupBindService.selectByPk(groupId);
        if (bindEntities == null || bindEntities.isEmpty()) {
            return new LinkedList<>();
        }
        return menuService.selectByPk(bindEntities.stream()
                .map(MenuGroupBindEntity::getMenuId)
                .distinct()
                .collect(Collectors.toList()));
    }

    @Override
    @CacheEvict(allEntries = true)
    public String insert(MenuGroupEntity entity) {
        entity.setStatus((byte) 1);
        String id = super.insert(entity);
        List<MenuGroupBindEntity> bindEntities = entity.getBindInfo();
        if (bindEntities != null && !bindEntities.isEmpty()) {
            TreeSupportEntity.forEach(bindEntities, bindEntity -> {
                bindEntity.setGroupId(id);
                entity.setStatus((byte) 1);
            });
            menuGroupBindService.insertBatch(bindEntities);
        }
        return id;
    }

    @Override
    @CacheEvict(allEntries = true)
    public int updateByPk(MenuGroupEntity entity) {
        int size = super.updateByPk(entity);
        List<MenuGroupBindEntity> bindEntities = entity.getBindInfo();
        if (bindEntities != null && !bindEntities.isEmpty()) {
            TreeSupportEntity.forEach(bindEntities, bindEntity -> {
                bindEntity.setGroupId(entity.getId());
            });
            menuGroupBindService.deleteByGroupId(entity.getId());
            menuGroupBindService.insertBatch(bindEntities);
        }
        return size;
    }

    @CacheEvict(allEntries = true)
    @Override
    public int updateByPk(List<MenuGroupEntity> data) {
        return super.updateByPk(data);
    }

    @Override
    @CacheEvict(allEntries = true)
    public int updateByPk(String id, MenuGroupEntity entity) {
        return super.updateByPk(id, entity);
    }

    @Override
    @CacheEvict(allEntries = true)
    public MenuGroupEntity deleteByPk(String id) {
        return super.deleteByPk(id);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void enable(String id) {
        tryValidateProperty(StringUtils.hasLength(id), MenuGroupEntity.id, "{id_is_null}");
        createUpdate()
                .set(MenuGroupEntity.status, 1)
                .where(MenuGroupEntity.id, id)
                .exec();
    }

    @Override
    @CacheEvict(allEntries = true)
    public void disable(String id) {
        tryValidateProperty(StringUtils.hasLength(id), MenuGroupEntity.id, "{id_is_null}");
        DefaultDSLUpdateService
                .createUpdate(getDao())
                .set(MenuGroupEntity.status, 0)
                .where(MenuGroupEntity.id, id)
                .exec();
    }
}
