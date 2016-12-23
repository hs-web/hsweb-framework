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

package org.hswebframework.web.controller.config;

import org.hswebframework.web.authorization.Authorize;
import org.hswebframework.web.bean.config.ConfigBean;
import org.hswebframework.web.bean.config.SimpleConfigBean;
import org.hswebframework.web.commons.beans.param.QueryParamBean;
import org.hswebframework.web.controller.GenericBeanController;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@RestController
@RequestMapping("${hsweb.web.mappings.config:config}")
@Authorize(module = "config")
@AccessLogger("配置管理")
public class ConfigController implements GenericBeanController<SimpleConfigBean, String> {

    @Autowired
    private ConfigService<QueryParamBean> configService;

    @Override
    public ConfigService getService() {
        return configService;
    }

}
