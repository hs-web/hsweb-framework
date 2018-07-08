package org.hswebframework.web.controller.form;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ResponseHeader;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.param.DeleteParamEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.entity.param.UpdateParamEntity;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.service.form.DynamicFormOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 动态表单常用操作控制器,如增删改查
 *
 * @author zhouhao
 * @since 3.0
 */
@RestController
@Api(value = "动态表单操作", tags = "动态表单-数据操作")
@RequestMapping("/dynamic/form/operation")
@Authorize(permission = "dynamic-form-operation", description = "动态表单数据操作")
public class DynamicFormOperationController {

    private DynamicFormOperationService dynamicFormOperationService;

    @Autowired
    public void setDynamicFormOperationService(DynamicFormOperationService dynamicFormOperationService) {
        this.dynamicFormOperationService = dynamicFormOperationService;
    }

    @GetMapping("/{formId}")
    @ApiOperation("动态查询")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<PagerResult<Object>> selectPager(@PathVariable String formId, QueryParamEntity paramEntity) {
        return ResponseMessage.ok(dynamicFormOperationService.selectPager(formId, paramEntity));
    }

    @GetMapping("/{formId}/no-paging")
    @ApiOperation("不分页动态查询")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<List<Object>> selectNoPaging(@PathVariable String formId, QueryParamEntity paramEntity) {
        paramEntity.setPaging(false);
        return ResponseMessage.ok(dynamicFormOperationService.select(formId, paramEntity));
    }

    @GetMapping("/{formId}/single")
    @ApiOperation("动态查询返回单条数据")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<Object> selectSingle(@PathVariable String formId, QueryParamEntity paramEntity) {
        return ResponseMessage.ok(dynamicFormOperationService.selectSingle(formId, paramEntity));
    }

    @GetMapping("/{formId}/count")
    @ApiOperation("动态查询返回数据条数")
    @Authorize(action = Permission.ACTION_QUERY)
    public ResponseMessage<Object> selectCount(@PathVariable String formId, QueryParamEntity paramEntity) {
        return ResponseMessage.ok(dynamicFormOperationService.count(formId, paramEntity));
    }

    @PostMapping("/{formId}")
    @ApiOperation("新增")
    @Authorize(action = Permission.ACTION_ADD)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseMessage<Map<String, Object>> add(@PathVariable String formId,
                                                    @RequestBody Map<String, Object> data) {
        return ResponseMessage.ok(dynamicFormOperationService.insert(formId, data));
    }

    @PatchMapping("/{formId}")
    @ApiOperation("新增或者修改")
    @Authorize(action = {Permission.ACTION_ADD, Permission.ACTION_UPDATE}, logical = Logical.OR)
    public ResponseMessage<Object> saveOrUpdate(@PathVariable String formId,
                                                @RequestBody Map<String, Object> data) {
        return ResponseMessage.ok(dynamicFormOperationService.saveOrUpdate(formId, data));
    }

    @PutMapping("/{formId}")
    @ApiOperation("动态修改")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Integer> dynamicUpdate(@PathVariable String formId,
                                                  @RequestBody UpdateParamEntity<Map<String, Object>> paramEntity) {
        return ResponseMessage.ok(dynamicFormOperationService.update(formId, paramEntity));
    }

    @GetMapping("/{formId}/{id}")
    @ApiOperation("根据主键查询")
    @Authorize(action = Permission.ACTION_GET)
    public ResponseMessage<Map<String, Object>> selectById(@PathVariable String formId,
                                                           @PathVariable String id) {
        return ResponseMessage.ok(dynamicFormOperationService.selectById(formId, id));
    }

    @PutMapping("/{formId}/{id}")
    @ApiOperation("根据主键修改")
    @Authorize(action = Permission.ACTION_UPDATE)
    public ResponseMessage<Map<String, Object>> updateById(@PathVariable String formId,
                                                           @PathVariable String id,
                                                           @RequestBody Map<String, Object> param) {
        return ResponseMessage.ok(dynamicFormOperationService.updateById(formId, id, param));
    }

    @DeleteMapping("/{formId}/{id}")
    @ApiOperation("根据主键删除")
    @Authorize(action = Permission.ACTION_DELETE)
    public ResponseMessage<Integer> deleteById(@PathVariable String formId,
                                               @PathVariable String id) {
        return ResponseMessage.ok(dynamicFormOperationService.deleteById(formId, id));
    }
}
