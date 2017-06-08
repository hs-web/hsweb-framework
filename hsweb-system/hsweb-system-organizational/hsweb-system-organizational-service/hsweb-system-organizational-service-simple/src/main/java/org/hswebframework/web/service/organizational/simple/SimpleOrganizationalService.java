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
package org.hswebframework.web.service.organizational.simple;

import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.dao.organizational.OrganizationalDao;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.organizational.OrganizationalService;
import org.hswebframework.utils.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("organizationalService")
public class SimpleOrganizationalService extends AbstractTreeSortService<OrganizationalEntity, String>
        implements OrganizationalService {
    @Autowired
    private OrganizationalDao organizationalDao;

    @Override
    public OrganizationalDao getDao() {
        return organizationalDao;
    }

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public List<String> getAllCanUseRoleIds() {
        // TODO: 17-3-1
        return null;
    }

    @Override
    public List<String> getCanUseRoleIds(List<String> orgIds) {
        if (orgIds == null || orgIds.isEmpty()) return new ArrayList<>();
        return createQuery().where().in(GenericEntity.id, orgIds).listNoPaging()
                .stream().map(OrganizationalEntity::getOptionalRoles) //得到机构可选角色id集合
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
