/*
 * Copyright 2015-2016 https://github.com/hs-web
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

package org.hsweb.web.controller.form;

import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.form.FormService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

/**
 * 动态表单控制器,用于管理动态表单
 *
 * @author zhouhao
 */
@RestController
@RequestMapping(value = "/form")
@AccessLogger("表单管理")
@Authorize(module = "form")
public class FormController extends GenericController<Form, String> {
    /**
     * 表单服务类
     */
    @Resource
    private FormService formService;

    @Override
    public FormService getService() {
        return this.formService;
    }

    /**
     * 获取最新版本的表单列表
     *
     * @param param 查询参数
     * @return {@link GenericController#list(QueryParam)}
     */
    @RequestMapping(value = "/~latest", method = RequestMethod.GET)
    @AccessLogger("获取最新版列表")
    public ResponseMessage latestList(QueryParam param) {
        ResponseMessage message;
        if (!param.isPaging()) {
            message = ResponseMessage.ok(formService.selectLatestList(param));
        } else {
            param.setPaging(false);
            int total = formService.countLatestList(param);
            param.rePaging(total);
            List<Form> list = formService.selectLatestList(param);
            PagerResult<Form> result = new PagerResult<>();
            result.setData(list).setTotal(total);
            message = ResponseMessage.ok(result);
        }
        message.include(Form.class, param.getIncludes())
                .exclude(Form.class, param.getExcludes())
                .onlyData();
        return message;
    }

    /**
     * 获取指定名称表单的最新版
     *
     * @param name 表单名称
     * @return 表单信息
     * @throws NotFoundException 表单不存在
     */
    @RequestMapping(value = "/{name}/latest", method = RequestMethod.GET)
    public ResponseMessage latest(@PathVariable(value = "name") String name) {
        Form form = formService.selectLatest(name);
        assertFound(form, "表单不存在");
        return ResponseMessage.ok(form);
    }

    /**
     * 获取指定名称和版本的表单
     *
     * @param name    表单名称
     * @param version 版本
     * @return 表单信息
     * @throws NotFoundException 表单不存在
     */
    @RequestMapping(value = "/{name}/{version}", method = RequestMethod.GET)
    public ResponseMessage version(@PathVariable(value = "name") String name,
                                   @PathVariable(value = "version") Integer version) {
        Form form = formService.selectByVersion(name, version);
        assertFound(form, "表单不存在");
        return ResponseMessage.ok(form);
    }

    /**
     * 发布指定id的表单
     *
     * @param id 表单id
     * @return 发布结果
     * @throws SQLException      部署执行sql错误
     * @throws NotFoundException 表单不存在
     */
    @RequestMapping(value = "/{id}/deploy", method = RequestMethod.PUT)
    @Authorize(action = "deploy")
    public ResponseMessage deploy(@PathVariable("id") String id) throws SQLException {
        formService.deploy(id);
        return ResponseMessage.ok();
    }

    /**
     * 卸载发布指定id的表单
     *
     * @param id 表单id
     * @return 卸载结果
     * @throws NotFoundException 表单不存在
     */
    @RequestMapping(value = "/{id}/unDeploy", method = RequestMethod.PUT)
    @Authorize(action = "deploy")
    public ResponseMessage unDeploy(@PathVariable("id") String id) {
        formService.unDeploy(id);
        return ResponseMessage.ok();
    }

    /**
     * 获取已经发布表单的html
     *
     * @param name 表单名称
     * @return html内容
     * @throws NotFoundException 表单不存在
     */
    @RequestMapping(value = "/{name}/html", method = RequestMethod.GET)
    public ResponseMessage html(@PathVariable("name") String name) {
        return ResponseMessage.ok(formService.createDeployHtml(name));
    }

    /**
     * 创建一个指定表单ID的新版本
     *
     * @param id 表单ID
     * @return 新版本表单的ID
     * @throws NotFoundException 表单不存在
     */
    @RequestMapping(value = "/{id}/new-version", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseMessage newVersion(@PathVariable("id") String id) {
        return ResponseMessage.created(formService.createNewVersion(id));
    }

    /**
     * 获取指定名称并且正在使用中的表单
     *
     * @param name 表单名称
     * @throws NotFoundException 表单不存
     */
    @RequestMapping(value = "/{name}/using", method = RequestMethod.GET)
    public ResponseMessage using(@PathVariable("name") String name) {
        Form form = formService.selectUsing(name);
        assertFound(form, "表单不存在");
        return ResponseMessage.ok(form).exclude(Form.class, "html");
    }

    /**
     * 获取指定id表单的html
     *
     * @param id 表单ID
     * @return html
     * @throws NotFoundException 表单不存在
     */
    @RequestMapping(value = "/{id}/view", method = RequestMethod.GET)
    public ResponseMessage view(@PathVariable("id") String id) throws Exception {
        return ResponseMessage.ok(formService.createViewHtml(id));
    }
}
