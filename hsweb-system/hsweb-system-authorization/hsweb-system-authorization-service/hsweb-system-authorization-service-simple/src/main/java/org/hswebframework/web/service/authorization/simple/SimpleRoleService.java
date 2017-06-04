/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.dao.authorization.PermissionRoleDao;
import org.hswebframework.web.dao.authorization.RoleDao;
import org.hswebframework.web.entity.authorization.PermissionRoleEntity;
import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.entity.authorization.bind.BindPermissionRoleEntity;
import org.hswebframework.web.service.AbstractService;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.hswebframework.web.service.authorization.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hswebframework.web.service.authorization.simple.CacheConstants.USER_AUTH_CACHE_NAME;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Transactional(rollbackFor = Throwable.class)
@Service("roleService")
public class SimpleRoleService extends AbstractService<RoleEntity, String>
        implements RoleService, DefaultDSLQueryService<RoleEntity, String> {

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PermissionRoleDao permissionRoleDao;

    @Override
    public RoleDao getDao() {
        return roleDao;
    }

    protected <T extends PermissionRoleEntity> void syncPermissions(String roleId, List<T> permissionRoleEntities) {
        if (permissionRoleEntities == null) return;
        permissionRoleEntities.forEach(permission -> {
            permission.setRoleId(roleId);
            permissionRoleDao.insert(permission);
        });
    }

    @Override
    public <T extends PermissionRoleEntity> String insert(BindPermissionRoleEntity<T> roleEntity) {
        tryValidateProperty(StringUtils.hasLength(roleEntity.getId()), RoleEntity.id, "id {not_be_null}");
        tryValidateProperty(null == selectByPk(roleEntity.getId()), RoleEntity.id, "{role_exists}");
        roleEntity.setEnabled(true);
        tryValidate(roleEntity);
        roleDao.insert(roleEntity);
        syncPermissions(roleEntity.getId(), roleEntity.getPermissions());
        return roleEntity.getId();
    }

    @Override
    public <T extends PermissionRoleEntity> void updateByPrimaryKey(BindPermissionRoleEntity<T> roleEntity) {
        tryValidateProperty(StringUtils.hasLength(roleEntity.getId()), RoleEntity.id, "id {not_be_null}");
        roleEntity.setEnabled(null);
        tryValidate(roleEntity);
        DefaultDSLUpdateService
                .createUpdate(roleDao, roleEntity)
                .where(GenericEntity.id, roleEntity.getId());
    }

    @Override
    public void enable(String roleId) {
        tryValidateProperty(StringUtils.hasLength(roleId), RoleEntity.id, "{id_is_null}");
        DefaultDSLUpdateService.createUpdate(getDao())
                .set(RoleEntity.enabled, true)
                .where(RoleEntity.id, roleId)
                .exec();
    }

    @Override
    public void disable(String roleId) {
        tryValidateProperty(StringUtils.hasLength(roleId), RoleEntity.id, "{id_is_null}");
        DefaultDSLUpdateService.createUpdate(getDao())
                .set(RoleEntity.enabled, false)
                .where(RoleEntity.id, roleId)
                .exec();
    }

    @Override
    @SuppressWarnings("unchecked")
    public RoleEntity selectByPk(String roleId) {
        tryValidateProperty(StringUtils.hasLength(roleId), RoleEntity.id, "{id_is_null}");
        return createQuery().where(RoleEntity.id, roleId).single();
    }

    @Override
    public List<RoleEntity> selectByPk(List<String> id) {
        tryValidateProperty(id == null || id.isEmpty(), RoleEntity.id, "{id_is_null}");
        return createQuery().where().in(RoleEntity.id, id).listNoPaging();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends PermissionRoleEntity> BindPermissionRoleEntity<T> selectDetailByPk(String roleId) {
        RoleEntity entity = createQuery().where(RoleEntity.id, roleId).single();
        if (entity == null) return null;
        BindPermissionRoleEntity<T> bindPermissionRoleEntity =
                entityFactory.newInstance(BindPermissionRoleEntity.class, entity);

        bindPermissionRoleEntity.setPermissions(new ArrayList(permissionRoleDao.selectByRoleId(roleId)));
        return bindPermissionRoleEntity;
    }

    @Override
    @CacheEvict(value = USER_AUTH_CACHE_NAME, allEntries = true)
    public <T extends PermissionRoleEntity> boolean update(BindPermissionRoleEntity<T> roleEntity) {
        tryValidateProperty(StringUtils.hasLength(roleEntity.getId()), RoleEntity.id, "id {not_be_null}");
        tryValidateProperty(null == selectByPk(roleEntity.getId()), RoleEntity.id, "{role_not_exists}");
        tryValidate(roleEntity);
        DefaultDSLUpdateService.createUpdate(roleDao)
                .set(RoleEntity.name, roleEntity.getName())
                .set(RoleEntity.describe, roleEntity.getDescribe())
                .where(RoleEntity.id, roleEntity.getId()).exec();
        if (roleEntity.getPermissions() != null) {
            permissionRoleDao.deleteByRoleId(roleEntity.getId());
            syncPermissions(roleEntity.getId(), roleEntity.getPermissions());
        }
        return true;
    }
}
