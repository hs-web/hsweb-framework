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

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.InsertService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * 通用新增控制器<br>
 * 使用:实现该接口,注解@RestController 以及@RequestMapping("/myController")
 * 客户端调用: 通过POST请求,contentType为application/json 。参数为E泛型的json格式
 * <code>
 * curl -l -H "Content-type: application/json" -X POST -d '{"field1":"value1","field2":"value2"}'
 * http://domain/contextPath/myController
 * </code>
 *
 * @author zhouhao
 * @since 3.0
 */
public interface CreateController<E, PK>  {

    InsertService<E, PK> getService();

    @Authorize(action = "add")
    @PostMapping
    @AccessLogger("添加数据")
    @ResponseStatus(HttpStatus.CREATED)
    default ResponseMessage add(@RequestBody E data) {
        return ok(getService().insert(data));
    }
}
