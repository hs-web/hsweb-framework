package org.hswebframework.web.controller.organizational;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.SimpleGenericEntityController;
import org.hswebframework.web.entity.organizational.DistrictEntity;
import org.hswebframework.web.logging.AccessLogger;
import  org.hswebframework.web.service.organizational.DistrictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  表单发布日志
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
}
