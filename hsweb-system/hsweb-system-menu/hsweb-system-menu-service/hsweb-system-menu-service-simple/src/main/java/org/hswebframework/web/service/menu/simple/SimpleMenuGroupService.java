/*
 *  Copyright 2016 http://www.hswebframework.org
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
package org.hswebframework.web.service.menu.simple;

import org.hswebframework.web.dao.menu.MenuGroupDao;
import org.hswebframework.web.entity.authorization.PermissionRoleEntity;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.entity.authorization.bind.BindPermissionRoleEntity;
import org.hswebframework.web.entity.menu.MenuEntity;
import org.hswebframework.web.entity.menu.MenuGroupBindEntity;
import org.hswebframework.web.entity.menu.MenuGroupEntity;
import org.hswebframework.web.entity.menu.SimpleMenuGroupEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.hswebframework.web.service.authorization.RoleService;
import org.hswebframework.web.service.menu.MenuGroupBindService;
import org.hswebframework.web.service.menu.MenuGroupService;
import org.hswebframework.web.service.menu.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.hswebframework.web.service.menu.simple.CacheConstants.MENU_CACHE_NAME;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("menuGroupService")
@CacheConfig(cacheNames = MENU_CACHE_NAME)
public class SimpleMenuGroupService
        extends AbstractTreeSortService<MenuGroupEntity, String>
        implements MenuGroupService {
    @Autowired
    private MenuGroupDao menuGroupDao;

    @Autowired(required = false)
    private RoleService roleService;

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

    private boolean checkRoleServiceIsEnable() {
        if (roleService == null) {
            logger.warn("roleService为空,将不会自动同步角色信息!");
            return false;
        }
        return true;
    }

    @Cacheable(key = "'group-id-list:'+#groupId==null?0:#groupId.hashCode()")
    public List<MenuEntity> getMenuByGroupId(List<String> groupId) {
        List<MenuGroupBindEntity> bindEntities = menuGroupBindService.selectByPk(groupId);
        if (bindEntities == null || bindEntities.isEmpty()) return new LinkedList<>();
        return menuService.selectByPk(bindEntities.stream()
                .map(MenuGroupBindEntity::getMenuId)
                .distinct()
                .collect(Collectors.toList()));
    }

    @CacheEvict(allEntries = true)
    public String insert(MenuGroupEntity entity) {
        String id = super.insert(entity);
        List<MenuGroupBindEntity> bindEntities = entity.getBindInfo();
        if (bindEntities != null && !bindEntities.isEmpty()) {
            bindEntities.forEach(bind -> bind.setGroupId(entity.getId()));
            menuGroupBindService.insertBatch(bindEntities);
        }
        trySyncRoleInfo(entity);
        return id;
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
    public int deleteByPk(String id) {
        return super.deleteByPk(id);
    }

    @Override
    public String saveOrUpdateForSingle(MenuGroupEntity entity) {
        String id = super.saveOrUpdateForSingle(entity);
        trySyncRoleInfo(entity);
        return id;
    }

    @Override
    @CacheEvict(allEntries = true)
    public void enable(String id) {
        tryValidateProperty(StringUtils.hasLength(id), MenuGroupEntity.id, "{id_is_null}");
        createUpdate()
                .set(MenuGroupEntity.enabled, true)
                .where(MenuGroupEntity.id, id)
                .exec();
        if (checkRoleServiceIsEnable())
            roleService.enable(id);
    }

    @Override
    @CacheEvict(allEntries = true)
    public void disable(String id) {
        tryValidateProperty(StringUtils.hasLength(id), MenuGroupEntity.id, "{id_is_null}");
        DefaultDSLUpdateService
                .createUpdate(getDao())
                .set(MenuGroupEntity.enabled, false)
                .where(MenuGroupEntity.id, id)
                .exec();
        if (checkRoleServiceIsEnable())
            roleService.disable(id);
    }

    @SuppressWarnings("unchecked")
    protected void trySyncRoleInfo(MenuGroupEntity menuGroupEntity) {
        if (!checkRoleServiceIsEnable()) return;
        //角色的操作,新增or更新
        Consumer<BindPermissionRoleEntity<PermissionRoleEntity>> roleEntityConsumer =
                roleService.selectByPk(menuGroupEntity.getId()) == null
                        ? roleService::insert
                        : roleService::update;

        //设置属性
        BindPermissionRoleEntity<PermissionRoleEntity> roleEntity = entityFactory.newInstance(BindPermissionRoleEntity.class);
        roleEntity.setId(menuGroupEntity.getId());
        roleEntity.setName(menuGroupEntity.getName());
        roleEntity.setEnabled(menuGroupEntity.isEnabled());
        roleEntity.setDescribe(menuGroupEntity.getDescribe());
        List<MenuGroupBindEntity> bindEntities = menuGroupEntity.getBindInfo();
        if (bindEntities != null && bindEntities.size() > 0) {
            roleEntity.setPermissions(bindEntities.parallelStream()
                    .map(bind -> {
                                //转换 MenuGroupBindEntity 为PermissionRoleEntity
                                PermissionRoleEntity permission = entityFactory.newInstance(PermissionRoleEntity.class, bind);
                                permission.setRoleId(bind.getGroupId());
                                MenuEntity menuEntity = menuService.selectByPk(bind.getMenuId());
                                assertNotNull(menuEntity, "menu " + bind.getMenuId() + " not found");
                                permission.setPermissionId(menuEntity.getPermissionId());
                                return permission;
                            }
                    ).sorted()
                    .collect(Collectors.toList()));
        }

        roleEntityConsumer.accept(roleEntity);
    }

}
