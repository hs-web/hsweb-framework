package org.hswebframework.web.controller.organizational;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.organizational.DistrictEntity;
import org.hswebframework.web.entity.organizational.OrganizationalEntity;
import org.hswebframework.web.logging.AccessLogger;
import org.hswebframework.web.service.organizational.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 行政区划管理
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.district:district}")
@Authorize(permission = "district", description = "行政区划管理")
@Api(value = "行政区划管理", tags = "组织架构-行政区划管理")
public class DistrictController implements SimpleGenericEntityController<DistrictEntity, String, QueryParamEntity> {

    private DistrictService districtService;

    @Autowired
    public void setDistrictService(DistrictService districtService) {
        this.districtService = districtService;
    }

    @Override
    public DistrictService getService() {
        return districtService;
    }

    @GetMapping("/code/{code}")
    @Authorize(action = Permission.ACTION_QUERY)
    @ApiOperation("根据行政区划代码获取")
    public ResponseMessage<DistrictEntity> getByCode(@PathVariable String code) {
        return ResponseMessage.ok(districtService.selectByCode(code));
    }

    @GetMapping("/children/{parentId}")
    @Authorize(action = Permission.ACTION_QUERY)
    @ApiOperation("获取子级行政区划")
    public ResponseMessage<List<DistrictEntity>> getByParentId(@PathVariable String parentId) {
        return ResponseMessage.ok(districtService.selectChildNode(parentId));
    }

    @GetMapping("/children/{parentId}/all")
    @Authorize(action = Permission.ACTION_QUERY)
    @ApiOperation("获取所有子级行政区划")
    public ResponseMessage<List<DistrictEntity>> getAllByParentId(@PathVariable String parentId) {
        return ResponseMessage.ok(districtService.selectAllChildNode(parentId));
    }

    @GetMapping("/all")
    @Authorize(action = Permission.ACTION_QUERY)
    @ApiOperation("获取全部行政区划")
    public ResponseMessage<List<DistrictEntity>> all() {
        return ResponseMessage.ok(districtService.select());
    }

    @PatchMapping("/batch")
    @Authorize(action = Permission.ACTION_UPDATE)
    @ApiOperation("批量修改数据")
    public ResponseMessage<Void> updateBatch(@RequestBody List<DistrictEntity> batch) {
        districtService.updateBatch(batch);
        return ResponseMessage.ok();
    }

    @PutMapping("/{id}/disable")
    @Authorize(action = Permission.ACTION_DISABLE)
    @ApiOperation("禁用行政区划")
    public ResponseMessage<Boolean> disable(@PathVariable String id) {
        districtService.disable(id);
        return ResponseMessage.ok();
    }

    @PutMapping("/{id}/enable")
    @Authorize(action = Permission.ACTION_ENABLE)
    @ApiOperation("启用行政区划")
    public ResponseMessage<Boolean> enable(@PathVariable String id) {
        districtService.enable(id);
        return ResponseMessage.ok();
    }
}
