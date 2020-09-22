package org.hswebframework.web.system.authorization.defaults.webflux;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.crud.web.reactive.ReactiveTreeServiceQueryController;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.hswebframework.web.system.authorization.defaults.service.DefaultDimensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dimension")
@Authorize
@Resource(id = "dimension", name = "权限维度管理", group = "system")
@Tag(name = "权限维度管理")
public class WebFluxDimensionController implements ReactiveServiceCrudController<DimensionEntity, String>
        , ReactiveTreeServiceQueryController<DimensionEntity, String> {


    @Autowired
    private DefaultDimensionService defaultDimensionService;


    @Override
    public DefaultDimensionService getService() {
        return defaultDimensionService;
    }

}
