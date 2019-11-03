package org.hswebframework.web.system.authorization.defaults.webflux;

import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.system.authorization.defaults.service.DefaultPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/permission")
@Authorize
@Resource(id = "permission",name = "权限管理",group = "system")
public class WebFluxPermissionController implements ReactiveServiceCrudController<PermissionEntity,String> {

    @Autowired
    private DefaultPermissionService permissionService;

    @Override
    public ReactiveCrudService<PermissionEntity, String> getService() {
        return permissionService;
    }

    @PutMapping("/status/{status}")
    @QueryAction
    public Mono<Integer> changePermissionState(@PathVariable Byte status, @RequestBody List<String> idList) {

        return Mono.just(idList)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(list -> permissionService.createUpdate()
                        .set(PermissionEntity::getStatus, status)
                        .where()
                        .in(PermissionEntity::getId, list)
                        .execute())
                .defaultIfEmpty(0);

    }
}
