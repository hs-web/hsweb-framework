package org.hswebframework.web.controller.dashboard;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.dashboard.DashBoardConfigEntity;
import org.hswebframework.web.dashboard.DashBoardService;
import org.hswebframework.web.dashboard.executor.DashBoardExecutor;
import org.hswebframework.web.service.CrudService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dashboard")
@Api(tags = "仪表盘-配置", value = "仪表盘配置")
@Authorize(permission = "dashboard", description = "仪表盘配置")
public class DashBoardConfigController implements SimpleGenericEntityController<DashBoardConfigEntity, String, QueryParamEntity> {

    @Autowired
    DashBoardService dashBoardService;

    @Autowired
    DashBoardExecutor dashBoardExecutor;

    @Override
    public CrudService<DashBoardConfigEntity, String> getService() {
        return dashBoardService;
    }


    @Override
    public ResponseMessage<String> add(@RequestBody DashBoardConfigEntity data) {
        Authentication.current().ifPresent(a -> data.setCreatorId(a.getUser().getId()));
        return SimpleGenericEntityController.super.add(data);
    }

    @Override
    public ResponseMessage<String> saveOrUpdate(@RequestBody DashBoardConfigEntity data) {
        Authentication.current().ifPresent(a -> data.setCreatorId(a.getUser().getId()));
        return SimpleGenericEntityController.super.saveOrUpdate(data);
    }

    @GetMapping("{id}/execute")
    @Authorize(merge = false)
    @ApiOperation("执行仪表盘配置")
    public ResponseMessage<Object> execute(@PathVariable String id) {
        return ResponseMessage.ok(dashBoardExecutor.execute(dashBoardService.selectByPk(id), Authentication.current().orElse(null)));
    }
}
