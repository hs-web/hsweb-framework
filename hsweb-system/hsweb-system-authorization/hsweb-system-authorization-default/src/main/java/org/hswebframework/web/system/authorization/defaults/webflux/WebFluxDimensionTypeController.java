package org.hswebframework.web.system.authorization.defaults.webflux;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.web.reactive.ReactiveCrudController;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionTypeEntity;
import org.hswebframework.web.system.authorization.defaults.service.DefaultDimensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/dimension-type")
@Authorize
@Resource(id = "dimension", name = "权限维度管理", group = "system")
@Tag(name = "权限维度类型管理")
public class WebFluxDimensionTypeController implements ReactiveCrudController<DimensionTypeEntity, String> {

    @Autowired
    private List<DimensionProvider> dimensionProviders;

    @Autowired
    private ReactiveRepository<DimensionTypeEntity, String> reactiveRepository;

    @GetMapping("/all")
    @QueryAction
    @Operation(summary = "获取全部维度类型")
    public Flux<DimensionTypeResponse> findAllType() {
        return Flux.fromIterable(dimensionProviders)
                .flatMap(DimensionProvider::getAllType)
                .map(DimensionTypeResponse::of);
    }

    @Override
    public ReactiveRepository<DimensionTypeEntity, String> getRepository() {
        return reactiveRepository;
    }
}
