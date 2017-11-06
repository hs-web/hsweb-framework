package org.hswebframework.web.controller.organizational;

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
 * 表单发布日志
 *
 * @author hsweb-generator-online
 */
@RestController
@RequestMapping("${hsweb.web.mappings.district:district}")
@Authorize(permission = "district")
@AccessLogger("行政区域")
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

    @PatchMapping("/code/{code}")
    @Authorize(action = Permission.ACTION_QUERY)
    @AccessLogger("根据行政区划代码获取")
    public ResponseMessage<DistrictEntity> getByCode(@PathVariable String code) {
        return ResponseMessage.ok(districtService.selectByCode(code));
    }

    @PatchMapping("/all")
    @Authorize(action = Permission.ACTION_QUERY)
    @AccessLogger("获取全部行政区划")
    public ResponseMessage<List<DistrictEntity>> all() {
        return ResponseMessage.ok(districtService.select());
    }

    @PatchMapping("/batch")
    @Authorize(action = Permission.ACTION_UPDATE)
    @AccessLogger("批量修改数据")
    public ResponseMessage<Void> updateBatch(@RequestBody List<DistrictEntity> batch) {
        districtService.updateBatch(batch);
        return ResponseMessage.ok();
    }

    @PutMapping("/{id}/disable")
    @Authorize(action = Permission.ACTION_DISABLE)
    @AccessLogger("禁用机构")
    public ResponseMessage<Boolean> disable(@PathVariable String id) {
        districtService.disable(id);
        return ResponseMessage.ok();
    }

    @PutMapping("/{id}/enable")
    @Authorize(action = Permission.ACTION_ENABLE)
    @AccessLogger("启用机构")
    public ResponseMessage<Boolean> enable(@PathVariable String id) {
        districtService.enable(id);
        return ResponseMessage.ok();
    }
}
