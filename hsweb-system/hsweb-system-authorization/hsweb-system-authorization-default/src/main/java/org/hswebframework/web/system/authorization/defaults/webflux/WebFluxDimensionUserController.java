package org.hswebframework.web.system.authorization.defaults.webflux;

import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.DeleteAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.hswebframework.web.system.authorization.defaults.service.DefaultDimensionUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/dimension-user")
@Authorize
@Resource(id = "dimension", name = "权限维度管理", group = "system")
public class WebFluxDimensionUserController implements ReactiveServiceCrudController<DimensionUserEntity, String> {

    @Autowired
    private DefaultDimensionUserService dimensionUserService;

    @Override
    public ReactiveCrudService<DimensionUserEntity, String> getService() {
        return dimensionUserService;
    }


    @DeleteAction
    @DeleteMapping("/user/{userId}/dimension/{dimensionId}")
    public Mono<Integer> deleteByUserAndDimensionId(@PathVariable String userId, @PathVariable String dimensionId) {
        return dimensionUserService
                .createDelete()
                .where(DimensionUserEntity::getUserId, userId)
                .and(DimensionUserEntity::getDimensionId, dimensionId)
                .execute();
    }

    @DeleteAction
    @DeleteMapping("/user/{userId}")
    public Mono<Integer> deleteByUserId(@PathVariable String userId) {
        return dimensionUserService
                .createDelete()
                .where(DimensionUserEntity::getUserId, userId)
                .execute();
    }

    @DeleteAction
    @DeleteMapping("/dimension/{dimensionId}")
    public Mono<Integer> deleteByDimension(@PathVariable String dimensionId) {
        return dimensionUserService
                .createDelete()
                .where(DimensionUserEntity::getDimensionId, dimensionId)
                .execute();
    }
}
