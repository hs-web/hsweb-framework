package org.hswebframework.web.controller.form;

import io.swagger.annotations.Api;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.form.DynamicFormColumnBindEntity;
import org.hswebframework.web.entity.form.DynamicFormEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.form.DynamicFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 动态表单
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.dynamic/form:dynamic/form}")
@Authorize(permission = "dynamic-form")
@AccessLogger("动态表单")
@Api(tags = "dynamic-form", value = "动态表单")
public class DynamicFormController implements SimpleGenericEntityController<DynamicFormEntity, String, QueryParamEntity> {

    private DynamicFormService dynamicFormService;

    @Autowired
    public void setDynamicFormService(DynamicFormService dynamicFormService) {
        this.dynamicFormService = dynamicFormService;
    }

    @Override
    public DynamicFormService getService() {
        return dynamicFormService;
    }


    @PatchMapping("/bind")
    @AccessLogger("同时保存表单和字段")
    @Authorize(action = {Permission.ACTION_ADD, Permission.ACTION_UPDATE}, logical = Logical.OR)
    public ResponseMessage<String> saveOrUpdateFormAndColumn(@RequestBody DynamicFormColumnBindEntity bindEntity) {
        Authentication authentication = Authentication.current().orElse(null);
        Objects.requireNonNull(bindEntity.getForm(), "form can not be null");
        Objects.requireNonNull(bindEntity.getColumns(), "columns can not be null");

        if (null != authentication) {
            bindEntity.getForm().setCreatorId(authentication.getUser().getId());
        }
        bindEntity.getForm().setCreateTime(System.currentTimeMillis());

        return ResponseMessage.ok(dynamicFormService.saveOrUpdate(bindEntity));
    }

    @Override
    public ResponseMessage<String> add(@RequestBody DynamicFormEntity data) {
        Authentication authentication = Authentication.current().orElse(null);
        if (null != authentication) {
            data.setCreatorId(authentication.getUser().getId());
        }
        data.setCreateTime(System.currentTimeMillis());
        return SimpleGenericEntityController.super.add(data);
    }

    @PutMapping("/{id}/deploy")
    @Authorize(action = "deploy")
    @AccessLogger("发布表单")
    public ResponseMessage<Void> deploy(@PathVariable String id) {
        dynamicFormService.deploy(id);
        return ResponseMessage.ok();
    }

    @PutMapping("/{id}/un-deploy")
    @Authorize(action = "deploy")
    @AccessLogger("取消发布表单")
    public ResponseMessage<Void> unDeploy(@PathVariable String id) {
        dynamicFormService.unDeploy(id);
        return ResponseMessage.ok();
    }

    @GetMapping("/{id}/editing")
    @Authorize(action = "get")
    @AccessLogger("获取当前正在编辑的表单")
    public ResponseMessage<DynamicFormColumnBindEntity> getEditing(@PathVariable String id) {
        return ResponseMessage.ok(dynamicFormService.selectEditing(id));
    }

    @GetMapping("/{id}/latest")
    @Authorize(action = "get")
    @AccessLogger("获取最新发布的表单")
    public ResponseMessage<DynamicFormColumnBindEntity> selectDeployed(@PathVariable String id) {
        return ResponseMessage.ok(dynamicFormService.selectLatestDeployed(id));
    }

    @GetMapping("/{id}/{version:\\d+}")
    @Authorize(action = "get")
    @AccessLogger("获取指定版本的表单")
    public ResponseMessage<DynamicFormColumnBindEntity> selectDeployed(@PathVariable String id, @PathVariable int version) {
        return ResponseMessage.ok(dynamicFormService.selectDeployed(id, version));
    }
}
