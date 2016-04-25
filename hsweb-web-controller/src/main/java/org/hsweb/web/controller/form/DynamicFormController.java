package org.hsweb.web.controller.form;

import org.hsweb.web.bean.common.InsertMapParam;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateMapParam;
import org.hsweb.web.logger.annotation.AccessLogger;
import org.hsweb.web.message.ResponseMessage;
import org.hsweb.web.service.form.DynamicFormService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Created by zhouhao on 16-4-23.
 */
@RestController
@RequestMapping(value = "/dyn-form")
@AccessLogger("动态表单")
public class DynamicFormController {

    @Resource
    private DynamicFormService dynamicFormService;

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    @AccessLogger("查看列表")
    public ResponseMessage list(@PathVariable("name") String name,
                                @RequestParam(required = false) QueryParam param) {
        try {
            // 获取条件查询
            Object data;
            if (!param.isPaging())//不分页
                data = dynamicFormService.select(name, param);
            else
                data = dynamicFormService.selectPager(name, param);
            return new ResponseMessage(true, data)
                    .include(Map.class, param.getIncludes())
                    .exclude(Map.class, param.getExcludes())
                    .onlyData();
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    @RequestMapping(value = "/{name}/{primaryKey}", method = RequestMethod.GET)
    @AccessLogger("按主键查询")
    public ResponseMessage info(@PathVariable("name") String name,
                                @PathVariable("primaryKey") String primaryKey) {
        try {
            Map<String, Object> data = dynamicFormService.selectByPk(name, primaryKey);
            return new ResponseMessage(true, data);
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.POST)
    @AccessLogger("新增数据")
    public ResponseMessage insert(@PathVariable("name") String name,
                                  @RequestBody(required = true) Map<String, Object> data) {
        try {
            String pk = dynamicFormService.insert(name, new InsertMapParam(data));
            return new ResponseMessage(true, pk);
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    @RequestMapping(value = "/{name}/{primaryKey}", method = RequestMethod.PUT)
    @AccessLogger("更新数据")
    public ResponseMessage update(@PathVariable("name") String name,
                                  @PathVariable("primaryKey") String primaryKey,
                                  @RequestBody(required = true) Map<String, Object> data) {
        try {
            int i = dynamicFormService.updateByPk(name, primaryKey, new UpdateMapParam(data));
            return new ResponseMessage(true, i);
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

    @RequestMapping(value = "/{name}/{primaryKey}", method = RequestMethod.DELETE)
    @AccessLogger("删除数据")
    public ResponseMessage delete(@PathVariable("name") String name,
                                  @PathVariable("primaryKey") String primaryKey) {
        try {
            boolean success = dynamicFormService.deleteByPk(name, primaryKey);
            return new ResponseMessage(true, success);
        } catch (Exception e) {
            return new ResponseMessage(false, e);
        }
    }

}
