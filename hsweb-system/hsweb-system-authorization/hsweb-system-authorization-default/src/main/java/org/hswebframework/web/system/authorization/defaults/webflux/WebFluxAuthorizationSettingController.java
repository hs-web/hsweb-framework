package org.hswebframework.web.system.authorization.defaults.webflux;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.defaults.configuration.PermissionProperties;
import org.hswebframework.web.system.authorization.defaults.service.DefaultAuthorizationSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/autz-setting")
@Authorize
@Resource(id = "autz-setting", name = "权限分配", group = "system")
@Tag(name = "权限分配")
public class WebFluxAuthorizationSettingController implements ReactiveServiceCrudController<AuthorizationSettingEntity, String> {

    @Autowired
    private DefaultAuthorizationSettingService settingService;

    @Autowired
    private PermissionProperties permissionProperties;

    @Override
    public ReactiveCrudService<AuthorizationSettingEntity, String> getService() {
        return settingService;
    }

    @Override
    public AuthorizationSettingEntity applyAuthentication(AuthorizationSettingEntity entity,
                                                          Authentication authentication) {
        AuthorizationSettingEntity setting = ReactiveServiceCrudController.super.applyAuthentication(entity, authentication);

        return permissionProperties
                .getFilter()
                .handleSetting(authentication, setting);
    }
}
