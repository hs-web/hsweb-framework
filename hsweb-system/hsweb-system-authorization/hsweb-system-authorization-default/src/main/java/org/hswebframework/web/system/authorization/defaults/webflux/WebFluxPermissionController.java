package org.hswebframework.web.system.authorization.defaults.webflux;

import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.exception.NotFoundException;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/permission")
@Authorize(permission = "permission", description = "权限管理")
public class WebFluxPermissionController {

    @Autowired
    private ReactiveRepository<PermissionEntity, String> repository;

    @GetMapping
    @Authorize(action = Permission.ACTION_QUERY)
    public Flux<PermissionEntity> findAllPermission(QueryParamEntity paramEntity) {
        return repository.createQuery()
                .setParam(paramEntity)
                .fetch();
    }

    @GetMapping("/{id}")
    @Authorize(action = Permission.ACTION_QUERY)
    public Mono<PermissionEntity> getPermission(@PathVariable String id) {
        return Mono.just(id)
                .as(repository::findById)
                .switchIfEmpty(Mono.error(NotFoundException::new));
    }

    @GetMapping("/count")
    @Authorize(action = Permission.ACTION_QUERY)
    public Mono<Integer> countAllPermission(QueryParamEntity paramEntity) {
        return repository.createQuery()
                .setParam(paramEntity)
                .count()
                .defaultIfEmpty(0);
    }

    @PatchMapping
    @Authorize(action = Permission.ACTION_UPDATE)
    public Mono<SaveResult> savePermission(@RequestBody Publisher<PermissionEntity> paramEntity) {
        return repository.save(paramEntity);
    }

    @PutMapping("/status/{status}")
    @Authorize(action = Permission.ACTION_UPDATE)
    public Mono<Integer> changePermissionState(@PathVariable Byte status, @RequestBody List<String> idList) {

        return Mono.just(idList)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(list -> repository.createUpdate()
                        .set(PermissionEntity::getStatus, status)
                        .where()
                        .in(PermissionEntity::getId, list)
                        .execute())
                .defaultIfEmpty(0);

    }
}
