package org.hswebframework.web.system.authorization.defaults.webflux;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.hswebframework.web.api.crud.entity.QueryNoPagingOperation;
import org.hswebframework.web.api.crud.entity.QueryOperation;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.*;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.system.authorization.defaults.configuration.PermissionProperties;
import org.hswebframework.web.system.authorization.defaults.service.DefaultPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/permission")
@Authorize
@Resource(id = "permission", name = "权限管理", group = "system")
@Tag(name = "权限管理")
public class WebFluxPermissionController implements ReactiveServiceCrudController<PermissionEntity, String> {

    @Autowired
    private DefaultPermissionService permissionService;

    @Autowired
    private PermissionProperties permissionProperties;

    @Override
    public ReactiveCrudService<PermissionEntity, String> getService() {
        return permissionService;
    }

    @PutMapping("/status/{status}")
    @SaveAction
    @Operation(summary = "批量修改权限状态")
    public Mono<Integer> changePermissionState(@PathVariable @Parameter(description = "状态值:0禁用,1启用.") Byte status,
                                               @RequestBody List<String> idList) {

        return Mono.just(idList)
                   .filter(CollectionUtils::isNotEmpty)
                   .flatMap(list -> permissionService
                           .createUpdate()
                           .set(PermissionEntity::getStatus, status)
                           .where()
                           .in(PermissionEntity::getId, list)
                           .execute())
                   .defaultIfEmpty(0);

    }

    @GetMapping("/_query/for-grant")
    @ResourceAction(id = "grant", name = "赋权")
    @QueryNoPagingOperation(summary = "获取用于赋权的权限列表")
    public Flux<PermissionEntity> queryForGrant(QueryParamEntity query) {
        return Authentication
                .currentReactive()
                .flatMapMany(auth -> permissionProperties
                        .getFilter()
                        .doFilter(permissionService.query(query.noPaging()), auth));
    }
}
