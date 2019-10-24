package org.hswebframework.web.system.authorization.defaults.webflux;

import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.hswebframework.web.system.authorization.defaults.service.DefaultDimensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/dimension")
@Authorize
@Resource(id = "dimension", name = "权限维度管理", group = "system")
public class WebFluxDimensionController implements ReactiveServiceCrudController<DimensionEntity, String> {

    @Autowired
    private List<DimensionProvider> dimensionProviders;

    @Autowired
    private DefaultDimensionService defaultDimensionService;

    @GetMapping("/_query/tree")
    @QueryAction
    public Mono<List<DimensionEntity>> findAllTree(QueryParamEntity paramEntity) {
        return defaultDimensionService.queryResultToTree(Mono.just(paramEntity));
    }

    @Override
    public DefaultDimensionService getService() {
        return defaultDimensionService;
    }

    @GetMapping("/types")
    @QueryAction
    public Flux<DimensionType> findAllType() {
        return Flux.fromIterable(dimensionProviders)
                .flatMap(DimensionProvider::getAllType);
    }
}
