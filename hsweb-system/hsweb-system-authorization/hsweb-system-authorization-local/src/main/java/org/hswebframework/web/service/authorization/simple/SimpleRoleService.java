/*
 * Copyright 2019 http://www.hswebframework.org
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

import org.hswebframework.web.service.authorization.RoleService;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.dao.authorization.RoleDao;
import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.hswebframework.web.service.GenericEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Service("roleService")
public class SimpleRoleService extends GenericEntityService<RoleEntity, String> implements RoleService {

    @Autowired
    private RoleDao roleDao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public RoleDao getDao() {
        return roleDao;
    }

    @Override
    public String insert(RoleEntity entity) {
        entity.setStatus(DataStatus.STATUS_ENABLED);
        return super.insert(entity);
    }

    @Override
    public int updateByPk(String s, RoleEntity entity) {
        entity.setStatus(null);
        return super.updateByPk(s, entity);
    }

    @Override
    public void enable(String roleId) {
        tryValidateProperty(StringUtils.hasLength(roleId), RoleEntity.id, "{id_is_null}");
        DefaultDSLUpdateService.createUpdate(getDao())
                .set(RoleEntity.status, DataStatus.STATUS_ENABLED)
                .where(RoleEntity.id, roleId)
                .exec();
    }

    @Override
    public void disable(String roleId) {
        tryValidateProperty(StringUtils.hasLength(roleId), RoleEntity.id, "{id_is_null}");
        DefaultDSLUpdateService.createUpdate(getDao())
                .set(RoleEntity.status, DataStatus.STATUS_DISABLED)
                .where(RoleEntity.id, roleId)
                .exec();
    }
}
