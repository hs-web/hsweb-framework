package org.hswebframework.web.system.authorization.defaults.webflux;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.defaults.service.DefaultAuthorizationSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/autz-setting")
@Authorize
@Resource(id = "autz-setting",name = "权限分配",group = "system")
@Tag(name = "权限分配")
public class WebFluxAuthorizationSettingController implements ReactiveServiceCrudController<AuthorizationSettingEntity, String> {

    @Autowired
    private DefaultAuthorizationSettingService settingService;

    @Override
    public ReactiveCrudService<AuthorizationSettingEntity, String> getService() {
        return settingService;
    }
}
