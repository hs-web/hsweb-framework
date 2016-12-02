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

package org.hsweb.web.controller.form;

import org.hsweb.ezorm.core.OptionConverter;
import org.hsweb.ezorm.rdb.meta.RDBColumnMetaData;
import org.hsweb.ezorm.rdb.meta.RDBTableMetaData;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateMapParam;
import org.hsweb.web.bean.po.form.Form;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.service.form.DynamicFormService;
import org.hsweb.web.service.form.FormService;
import org.hsweb.web.service.resource.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态表单管理控制器,用于操作动态表单以及对表单数据的增删改查和excel导入导出
 * 将使用{@link org.hsweb.web.controller.DynamicFormAuthorizeValidator#validate(String, User, Map, String...)}进行权限验证
 *
 * @author zhouhao
 */
@RestController
@RequestMapping(value = "/dyn-form")
@AccessLogger("动态表单")
public class DynamicFormController {

    /**
     * 动态表单服务类
     */
    @Resource
    private DynamicFormService dynamicFormService;

    /**
     * 表单管理服务类
     */
    @Resource
    private FormService formService;

    /**
     * 文件服务类
     */
    @Resource
    private FileService fileService;

    /**
     * 根据表单名称,获取已发布此名称的表单信息
     *
     * @param name 表单名称
     * @return 表单数据
     * @throws NotFoundException 如果表达不存在或未部署将可能抛出此异常
     */
    @RequestMapping(value = "/deployed/{name}", method = RequestMethod.GET)
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'R')")
    @AccessLogger("查询发布表单")
    public ResponseMessage deployed(@PathVariable("name") String name) {
        return ResponseMessage.ok(formService.selectDeployed(name));
    }

    /**
     * 根据版本获取表单信息
     *
     * @param name    表单名称
     * @param version 版本
     * @return 表单信息
     * @throws NotFoundException 表单不存在时 抛出此异常
     */
    @RequestMapping(value = "/{name}/v/{version}", method = RequestMethod.GET)
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'R')")
    @AccessLogger("根据版本获取表单")
    public ResponseMessage selectByVersion(@PathVariable(value = "name") String name,
                                           @PathVariable(value = "version") Integer version) {
        Form form = formService.selectByVersion(name, version);
        if (form == null) throw new NotFoundException("表单不存在");
        return ResponseMessage.ok(form);
    }

    /**
     * 根据表单名称和查询参数,查询表单的数据列表
     *
     * @param name  表单名称
     * @param param 查询参数{@link QueryParam}
     * @return 如果查询参数指定不分页, 将返回格式:[{},{}...].如果指定分页,将返回:{total:数据总数,data:[{},{},...]}
     * @throws SQLException      执行查询sql错误
     * @throws NotFoundException 表单不存在或在未发布
     */
    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    @AccessLogger("查看数据列表")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'R')")
    public ResponseMessage list(@PathVariable("name") String name,
                                QueryParam param) throws SQLException {
        // 获取条件查询
        Object data;
        if (!param.isPaging())//不分页
            data = dynamicFormService.select(name, param);
        else
            data = dynamicFormService.selectPager(name, param);
        return ResponseMessage.ok(data)
                .onlyData();
    }


    /**
     * 根据表单名称和查询参数,查询表单的数据数量
     *
     * @param name  表单名称
     * @param param 查询参数{@link QueryParam}
     * @return 查询结果
     * @throws SQLException      执行查询sql错误
     * @throws NotFoundException 表单不存在或在未发布
     */
    @RequestMapping(value = "/{name}/total", method = RequestMethod.GET)
    @AccessLogger("查看数据数量")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'R')")
    public ResponseMessage total(@PathVariable("name") String name,
                                 QueryParam param) throws SQLException {
        return ResponseMessage.ok(dynamicFormService.total(name, param));
    }

    /**
     * 根据表单名和主键值查询数据
     *
     * @param name       表单名称
     * @param primaryKey 主键值
     * @return 查询结果
     * @throws SQLException      执行查询sql错误
     * @throws NotFoundException 表单不存在或在未发布
     */
    @RequestMapping(value = "/{name}/{primaryKey}", method = RequestMethod.GET)
    @AccessLogger("按主键查询数据")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'R')")
    public ResponseMessage info(@PathVariable("name") String name,
                                @PathVariable("primaryKey") String primaryKey) throws SQLException {
        Map<String, Object> data = dynamicFormService.selectByPk(name, primaryKey);
        return ResponseMessage.ok(data);
    }

    /**
     * 向指定名称的表单中新增一条数据
     *
     * @param name 表单名称
     * @param data 数据
     * @return 新增成功后返回被新增数据的主键值
     * @throws SQLException                                      执行查询sql错误
     * @throws NotFoundException                                 表单不存在或在未发布
     * @throws org.hsweb.ezorm.rdb.exception.ValidationException 数据格式验证失败时抛出此异常
     */
    @RequestMapping(value = "/{name}", method = RequestMethod.POST)
    @AccessLogger("新增数据")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'C')")
    public ResponseMessage insert(@PathVariable("name") String name,
                                  @RequestBody Map<String, Object> data) throws SQLException {
        String pk = dynamicFormService.insert(name, data);
        return ResponseMessage.ok(pk);
    }

    /**
     * 更新指定名称的表单中指定主键对应的数据
     *
     * @param name       表单名称
     * @param primaryKey 数据主键值
     * @param data       数据
     * @return 更新记录数量
     * @throws SQLException                                      执行查询sql错误
     * @throws NotFoundException                                 表单不存在或在未发布
     * @throws org.hsweb.ezorm.rdb.exception.ValidationException 数据格式验证失败时抛出此异常
     */
    @RequestMapping(value = "/{name}/{primaryKey}", method = RequestMethod.PUT)
    @AccessLogger("更新数据")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'U')")
    public ResponseMessage update(@PathVariable("name") String name,
                                  @PathVariable("primaryKey") String primaryKey,
                                  @RequestBody(required = true) Map<String, Object> data) throws SQLException {
        int i = dynamicFormService.updateByPk(name, primaryKey, new UpdateMapParam(data));
        return ResponseMessage.ok(i);
    }

    /**
     * 从指定名称的表单中根据主键值删除数据
     *
     * @param name       表单名称
     * @param primaryKey 主键值
     * @return 删除成功通知
     * @throws SQLException      执行查询sql错误
     * @throws NotFoundException 表单不存在或在未发布
     */
    @RequestMapping(value = "/{name}/{primaryKey}", method = RequestMethod.DELETE)
    @AccessLogger("删除数据")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'D')")
    public ResponseMessage delete(@PathVariable("name") String name,
                                  @PathVariable("primaryKey") String primaryKey) throws SQLException {
        dynamicFormService.deleteByPk(name, primaryKey);
        return ResponseMessage.ok();
    }

    /**
     * 从指定名称的表单中导出excel (.xlsx)
     *
     * @param name       表单名称
     * @param fileName   导出后的文件名
     * @param queryParam 导出数据查询参数{@link QueryParam}
     * @param response   {@link HttpServletResponse}
     * @throws Exception         excel导出异常
     * @throws SQLException      执行查询sql错误
     * @throws NotFoundException 表单不存在或在未发布
     */
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

    /**
     * 向指定名称的表单中导入excel数据.excel支持(xls,xlsx).
     * 如果某条数据导入失败,将不会回滚,而是记录错误原因.
     *
     * @param name   表单名称
     * @param fileId 文件id,通过{@link org.hsweb.web.controller.file.FileController#upload(MultipartFile[])} 上传后获得
     *               使用,分割可实现同时导入多个文件
     * @return 导入结果, 格式参照:{@link DynamicFormService#importExcel(String, InputStream)}
     * @throws IOException       读取excel文件错误
     * @throws BusinessException 解析excel文件错误
     * @throws NotFoundException 表单不存在或在未发布
     */
    @RequestMapping(value = "/{name}/import/{fileId:.+}", method = {RequestMethod.PATCH})
    @AccessLogger("导入excel")
    @Authorize(expression = "#dynamicFormAuthorizeValidator.validate(#name,#user,#paramsMap,'import')")
    public ResponseMessage importExcel(@PathVariable("name") String name,
                                       @PathVariable("fileId") String fileId) throws IOException {
        String[] ids = fileId.split("[,]");
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < ids.length; i++) {
            try (InputStream inputStream = fileService.readResources(ids[i])) {
                result.put(ids[i], dynamicFormService.importExcel(name, inputStream));
            }
        }
        return ResponseMessage.ok(result);
    }

    /**
     * 数据字典映射:将指定的数据映射为数据字典对应的数据。<br>
     * 如: 表单{name}的字段{field}的字典配置为 [{"男":"1"},{"女":"0"}];<br>
     * 传入参数type=1,data=男,得到结果 {data:"1"}。传入参数 type!=1,data=1.得到结果{data:"男"}
     *
     * @param name  表单名称
     * @param field 字段
     * @param data  要映射的数据
     * @param type  映射的类型 ，1或其他值，当为1时，将key映射为value，其他则将value映射为key。
     * @return 映射结果
     * @throws NotFoundException 表单或字段不存在
     */
    @RequestMapping(value = "/{name}/{field}/{type}/{data:.+}")
    @AccessLogger("数据字典映射")
    @Authorize
    public ResponseMessage mapperOption(@PathVariable("name") String name,
                                        @PathVariable("field") String field,
                                        @PathVariable("data") String data,
                                        @PathVariable("type") String type) {
        try {
            RDBTableMetaData metaData = dynamicFormService.getDefaultDatabase().getTable(name).getMeta();
            RDBColumnMetaData fieldMetaData = metaData.findColumn(field);
            if (fieldMetaData == null) throw new NullPointerException();
            OptionConverter converter = fieldMetaData.getOptionConverter();
            if (converter == null) return ResponseMessage.ok(data);
            switch (type) {
                case "1":
                    return ResponseMessage.ok(converter.converterData(data));
                default:
                    return ResponseMessage.ok(converter.converterValue(data));
            }
        } catch (NullPointerException e) {
            throw new NotFoundException("字段不存在");
        }
    }

}
