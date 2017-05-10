/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
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

package org.hswebframework.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.CrudService;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 通用实体的增删改查控制器
 *
 * @author zhouhao
 * @see GenericEntity
 * @see CrudController
 * @see CrudService
 */
public interface SimpleGenericEntityController<E extends GenericEntity<PK>, PK, Q extends Entity>
        extends SimpleCrudController<E, PK, Q> {

    CrudService<E, PK> getService();

    @Authorize(action = {Permission.ACTION_UPDATE, Permission.ACTION_ADD})
    @PatchMapping(path = "/{id}")
    @AccessLogger("{save_or_update}")
    @ApiOperation("根据ID修改数据,如果数据不存在则新增一条数据")
    @ApiResponses({
            @ApiResponse(code = 200, message = "修改(新增)成功,返回数据ID"),
            @ApiResponse(code = 401, message = "未授权"),
            @ApiResponse(code = 403, message = "无权限"),
            @ApiResponse(code = 409, message = "存在重复的资源")
    })
    default ResponseMessage<PK> saveOrUpdate(@PathVariable PK id, @RequestBody E data) {
        data.setId(id);
        return ResponseMessage.ok(getService().saveOrUpdate(data));
    }

}
