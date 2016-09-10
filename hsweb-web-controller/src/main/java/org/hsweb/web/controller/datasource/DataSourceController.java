/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.controller.datasource;

import org.hsweb.web.bean.po.datasource.DataSource;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.datasource.DataSourceService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 数据源管理控制器
 * Created by hsweb-generator
 */
@RestController
@RequestMapping(value = "/datasource")
@AccessLogger("数据源管理")
@Authorize(module = "datasource")
public class DataSourceController extends GenericController<DataSource, String> {

    @Resource
    private DataSourceService dataSourceService;

    @Override
    public DataSourceService getService() {
        return this.dataSourceService;
    }

    @RequestMapping(value = "/enable/{id}", method = RequestMethod.PUT)
    @Authorize(action = "enable")
    @AccessLogger("启用")
    public ResponseMessage enable(@PathVariable("id") String id) {
        dataSourceService.enable(id);
        return ResponseMessage.ok();
    }

    @RequestMapping(value = "/disable/{id}", method = RequestMethod.PUT)
    @Authorize(action = "disable")
    @AccessLogger("禁用")
    public ResponseMessage disable(@PathVariable("id") String id) {
        dataSourceService.disable(id);
        return ResponseMessage.ok();
    }

}
