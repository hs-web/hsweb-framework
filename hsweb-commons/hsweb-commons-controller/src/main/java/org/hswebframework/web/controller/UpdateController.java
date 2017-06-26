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

package org.hswebframework.web.controller;


import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.CreateEntityService;
import org.hswebframework.web.service.UpdateService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 通用更新控制器
 *
 * @author zhouhao
 */
public interface UpdateController<E, PK, M> {
    <S extends UpdateService<E, PK> & CreateEntityService<E>> S getService();

    @Authorize(action = Permission.ACTION_UPDATE)
    @PutMapping(path = "/{id}")
    @AccessLogger("{update_by_primary_key}")
    @ApiOperation("根据ID修改数据")
    @ApiResponses({
            @ApiResponse(code = 200, message = "修改成功"),
            @ApiResponse(code = 401, message = "未授权"),
            @ApiResponse(code = 403, message = "无权限"),
            @ApiResponse(code = 404, message = "要修改的数据不存在"),
            @ApiResponse(code = 409, message = "存在重复的资源")
    })
    default ResponseMessage updateByPrimaryKey(@PathVariable PK id, @RequestBody M data) {
        E entity = getService().createEntity();
        return ResponseMessage.ok(getService().updateByPk(id, modelToEntity(data, entity)));
    }

    /**
     * 将model转为entity
     *
     * @param model
     * @param entity
     * @return 转换后的结果
     * @see org.hswebframework.web.commons.model.Model
     * @see org.hswebframework.web.commons.entity.Entity
     */
    E modelToEntity(M model, E entity);
}
