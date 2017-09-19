package org.hswebframework.web.controller.form;

import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 动态表单常用操作控制器,如增删改查
 *
 * @author zhouhao
 * @since 3.0
 */
@RestController
@AccessLogger("动态表单操作")
@RequestMapping("/dynamic/form/operation")
public class DynamicFormOperationController {

    private DynamicFormOperationService dynamicFormOperationService;

    @Autowired
    public void setDynamicFormOperationService(DynamicFormOperationService dynamicFormOperationService) {
        this.dynamicFormOperationService = dynamicFormOperationService;
    }

    @GetMapping("/{formId}")
    @AccessLogger("查询")
//    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<PagerResult<Object>> selectPager(@PathVariable String formId, QueryParamEntity paramEntity) {
        return ResponseMessage.ok(dynamicFormOperationService.selectPager(formId, paramEntity));
    }

    @PostMapping("/{formId}")
    @AccessLogger("新增")
    public ResponseMessage<Map<String, Object>> add(@PathVariable String formId, @RequestBody Map<String, Object> data) {
        dynamicFormOperationService.insert(formId, data);
        return ResponseMessage.ok(data);
    }

    @PutMapping("/{formId}")
    @AccessLogger("修改")
    public ResponseMessage<Integer> update(@PathVariable String formId, @RequestBody UpdateParamEntity<Map<String, Object>> paramEntity) {
        return ResponseMessage.ok(dynamicFormOperationService.update(formId, paramEntity));
    }

    @DeleteMapping("/{formId}")
    @AccessLogger("删除")
    public ResponseMessage<Integer> delete(@PathVariable String formId, DeleteParamEntity paramEntity) {
        return ResponseMessage.ok(dynamicFormOperationService.delete(formId, paramEntity));
    }
}
