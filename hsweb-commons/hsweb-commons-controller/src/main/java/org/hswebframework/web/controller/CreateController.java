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

package org.hswebframework.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.CreateEntityService;
import org.hswebframework.web.service.InsertService;
import org.hswebframework.web.validator.group.CreateGroup;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * 通用新增控制器<br>
 * 使用:实现该接口,注解@RestController 以及@RequestMapping("/myController")
 * 客户端调用: 通过POST请求,contentType为application/json 。参数为E泛型的json格式
 * <pre>
 * curl -l -H "Content-type: application/json" -X POST -d '{"field1":"value1","field2":"value2"}' http://domain/contextPath/myController
 * </pre>
 *
 * @author zhouhao
 * @since 3.0
 */
public interface CreateController<E, PK, M> {

    @Authorize(ignore = true)
    <S extends InsertService<E, PK> & CreateEntityService<E>> S getService();

    @Authorize(action = Permission.ACTION_ADD)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiOperation(value = "新增")
    default ResponseMessage<PK> add(@RequestBody M data) {
        E entity = getService().createEntity();
        return ok(getService().insert(modelToEntity(data, entity)));
    }

    @Authorize(ignore = true)
    E modelToEntity(M model, E entity);
}
