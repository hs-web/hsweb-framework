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

package org.hsweb.web.controller.classified;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.classified.Classified;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.classified.ClassifiedService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 分类控制器,用于管理系统中各种分类
 *
 * @author zhouhao
 */
@RestController
@RequestMapping(value = "/classified")
@Authorize(module = "classified")
public class ClassifiedController extends GenericController<Classified, String> {

    @Resource
    private ClassifiedService classifiedService;

    @Override
    public ClassifiedService getService() {
        return this.classifiedService;
    }

    /**
     * 根据类型查询分类 {@link GenericController#list(QueryParam)}
     * 已过时，在正式版中删除 todo
     *
     * @param type  分类类型
     * @param param 查询参数
     * @return 查询结果
     */
    @RequestMapping(value = "/byType/{type}")
    @Deprecated
    public ResponseMessage listByTypeOld(@PathVariable("type") String type, QueryParam param) {
        param.where("type", type);
        return list(param);
    }


    /**
     * 根据类型查询分类 {@link GenericController#list(QueryParam)}
     *
     * @param type  分类类型
     * @param param 查询参数
     * @return 查询结果
     */
    @RequestMapping(value = "/type/{type}")
    public ResponseMessage listByType(@PathVariable("type") String type, QueryParam param) {
        param.where("type", type);
        return list(param);
    }
}
