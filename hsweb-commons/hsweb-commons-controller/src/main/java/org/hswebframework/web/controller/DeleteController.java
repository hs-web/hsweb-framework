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
import org.hswebframework.web.service.DeleteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import static org.hswebframework.web.controller.message.ResponseMessage.ok;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface DeleteController<PK> extends HswebController {

    DeleteService<PK> getService();

    @Authorize(action = "delete")
    @DeleteMapping(path = "/{id}")
    @AccessLogger("根据主键删除数据")
    default ResponseMessage deleteByPrimaryKey(@PathVariable PK id) {
        return ok(getService().deleteByPk(id));
    }

}
