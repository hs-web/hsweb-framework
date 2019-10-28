package org.hswebframework.web.system.authorization.defaults.webflux;

import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.web.reactive.ReactiveCrudController;
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
@Authorize
@Resource(id = "permission",name = "权限管理",group = "system")
public class WebFluxPermissionController implements ReactiveCrudController<PermissionEntity,String> {

    @Autowired
    private ReactiveRepository<PermissionEntity, String> repository;

    @Override
    public ReactiveRepository<PermissionEntity, String> getRepository() {
        return repository;
    }

    @PutMapping("/status/{status}")
    @QueryAction
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
