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

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateMapParam;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormService;
import org.hsweb.web.service.resource.FileService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态表单管理控制器,用于操作动态表单以及对表单数据的增删改查和excel导入导出
 *
 * @author zhouhao
 */
@RestController
@RequestMapping(value = "/dyn-form")
@AccessLogger("动态表单")
public class DynamicFormController {

    @Resource
    private DynamicFormService dynamicFormService;

    @Resource
    private FormService formService;

    @Resource
    private FileService fileService;

    @RequestMapping(value = "/deployed/{name}", method = RequestMethod.GET)
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'R')")
    @AccessLogger("发布表单")
    public ResponseMessage deployed(@PathVariable("name") String name) {
        return ResponseMessage.ok(formService.selectDeployed(name));
    }

    @RequestMapping(value = "/{name}/v/{version}", method = RequestMethod.GET)
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'R')")
    @AccessLogger("根据版本获取表单")
    public ResponseMessage selectByVersion(@PathVariable(value = "name") String name,
                                           @PathVariable(value = "version") Integer version) {
        Form form = formService.selectByVersion(name, version);
        if (form == null) throw new NotFoundException("表单不存在");
        return ResponseMessage.ok(form);
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    @AccessLogger("查看列表")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'R')")
    public ResponseMessage list(@PathVariable("name") String name,
                                QueryParam param) throws Exception {
        // 获取条件查询
        Object data;
        if (!param.isPaging())//不分页
            data = dynamicFormService.select(name, param);
        else
            data = dynamicFormService.selectPager(name, param);
        return ResponseMessage.ok(data)
                .onlyData();
    }

    @RequestMapping(value = "/{name}/{primaryKey}", method = RequestMethod.GET)
    @AccessLogger("按主键查询")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'R')")
    public ResponseMessage info(@PathVariable("name") String name,
                                @PathVariable("primaryKey") String primaryKey) throws Exception {
        Map<String, Object> data = dynamicFormService.selectByPk(name, primaryKey);
        return ResponseMessage.ok(data);
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.POST)
    @AccessLogger("新增数据")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'C')")
    public ResponseMessage insert(@PathVariable("name") String name,
                                  @RequestBody(required = true) Map<String, Object> data) throws Exception {
        String pk = dynamicFormService.insert(name, data);
        return ResponseMessage.ok(pk);
    }

    @RequestMapping(value = "/{name}/{primaryKey}", method = RequestMethod.PUT)
    @AccessLogger("更新数据")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'U')")
    public ResponseMessage update(@PathVariable("name") String name,
                                  @PathVariable("primaryKey") String primaryKey,
                                  @RequestBody(required = true) Map<String, Object> data) throws Exception {
        int i = dynamicFormService.updateByPk(name, primaryKey, new UpdateMapParam(data));
        return ResponseMessage.ok(i);
    }

    @RequestMapping(value = "/{name}/{primaryKey}", method = RequestMethod.DELETE)
    @AccessLogger("删除数据")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'D')")
    public ResponseMessage delete(@PathVariable("name") String name,
                                  @PathVariable("primaryKey") String primaryKey) throws Exception {
        dynamicFormService.deleteByPk(name, primaryKey);
        return ResponseMessage.ok();
    }

    @RequestMapping(value = "/{name}/export/{fileName:.+}", method = RequestMethod.GET)
    @AccessLogger("导出excel")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'export')")
    public void exportExcel(@PathVariable("name") String name,
                            @PathVariable("fileName") String fileName,
                            QueryParam queryParam,
                            HttpServletResponse response) throws Exception {
        response.setHeader("Content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
        response.setContentType("application/vnd.ms-excel");
        dynamicFormService.exportExcel(name, queryParam, response.getOutputStream());
    }

    @RequestMapping(value = "/{name}/import/{fileId:.+}", method = {RequestMethod.PATCH})
    @AccessLogger("导入为excel")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'import')")
    public ResponseMessage importExcel(@PathVariable("name") String name,
                                       @PathVariable("fileId") String fileId) throws Exception {
        String[] ids = fileId.split("[,]");
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            InputStream inputStream = fileService.readResources(ids[i]);
            result.put(ids[i], dynamicFormService.importExcel(name, inputStream));
        }
        return ResponseMessage.ok(result);
    }

}
