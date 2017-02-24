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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

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
        tryValidateProperty(null == selectByPk(roleEntity.getId()), RoleEntity.id, "{role_exists}");
        roleEntity.setEnabled(null);
        tryValidate(roleEntity);
        DefaultDSLUpdateService
                .createUpdate(roleDao, roleEntity)
                .where(GenericEntity.id, roleEntity.getId());
    }

    @Override
    public boolean enable(String roleId) {
        return DefaultDSLUpdateService.createUpdate(getDao()).set("enabled", true).where(RoleEntity.id, roleId).exec() > 0;
    }

    @Override
    public boolean disable(String roleId) {
        return DefaultDSLUpdateService.createUpdate(getDao()).set("enabled", false).where(RoleEntity.id, roleId).exec() > 0;
    }

    @Override
    public RoleEntity selectByPk(String roleId) {
        return createQuery().where(RoleEntity.id, roleId).single();
    }

    @Override
    public <T extends PermissionRoleEntity> boolean update(BindPermissionRoleEntity<T> roleEntity) {
        tryValidateProperty(StringUtils.hasLength(roleEntity.getId()), RoleEntity.id, "id {not_be_null}");
        tryValidateProperty(null == selectByPk(roleEntity.getId()), RoleEntity.id, "{role_not_exists}");
        tryValidate(roleEntity);
        DefaultDSLUpdateService.createUpdate(roleDao)
                .set("name", roleEntity.getName())
                .set("describe", roleEntity.getDescribe())
                .where(RoleEntity.id, roleEntity.getId()).exec();
        if (roleEntity.getProperties() != null) {
            permissionRoleDao.deleteByRoleId(roleEntity.getId());
            syncPermissions(roleEntity.getId(), roleEntity.getPermissions());
        }
        return true;
    }
}
