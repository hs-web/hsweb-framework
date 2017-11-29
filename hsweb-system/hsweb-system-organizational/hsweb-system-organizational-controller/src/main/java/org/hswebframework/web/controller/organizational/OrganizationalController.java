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

package org.hswebframework.web.controller.organizational;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.GenericEntityController;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.organizational.DepartmentEntity;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
import org.hswebframework.web.entity.organizational.PersonEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.organizational.OrganizationalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 组织
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.organizational:organizational}")
@Authorize(permission = "organizational",description = "机构管理",dataAccess = @RequiresDataAccess)
@Api(value = "机构管理",tags = "组织架构-机构管理")
public class OrganizationalController implements SimpleGenericEntityController<OrganizationalEntity, String, QueryParamEntity> {

    private OrganizationalService organizationalService;

    @Autowired
    public void setOrganizationalService(OrganizationalService organizationalService) {
        this.organizationalService = organizationalService;
    }

    @Override
    public OrganizationalService getService() {
        return organizationalService;
    }

    @PatchMapping("/batch")
    @Authorize(action = Permission.ACTION_UPDATE)
    @ApiOperation("批量修改数据")
    public ResponseMessage<Void> updateBatch(@RequestBody List<OrganizationalEntity> batch) {
        organizationalService.updateBatch(batch);
        return ResponseMessage.ok();
    }

    @PutMapping("/{id}/disable")
    @Authorize(action = Permission.ACTION_DISABLE)
    @ApiOperation("禁用机构")
    public ResponseMessage<Boolean> disable(@PathVariable String id) {
        organizationalService.disable(id);
        return ResponseMessage.ok();
    }

    @PutMapping("/{id}/enable")
    @Authorize(action = Permission.ACTION_ENABLE)
    @ApiOperation("启用机构")
    public ResponseMessage<Boolean> enable(@PathVariable String id) {
        organizationalService.enable(id);
        return ResponseMessage.ok();
    }
}
