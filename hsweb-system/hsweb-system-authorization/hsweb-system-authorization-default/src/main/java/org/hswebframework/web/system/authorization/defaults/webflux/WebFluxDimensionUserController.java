package org.hswebframework.web.system.authorization.defaults.webflux;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.authorization.annotation.Resource;
import org.hswebframework.web.crud.web.reactive.ReactiveCrudController;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.hswebframework.web.system.authorization.defaults.service.DefaultDimensionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/dimension-user")
@Authorize
@Resource(id = "dimension",name = "权限维度管理",group = "system")
public class WebFluxDimensionUserController implements ReactiveCrudController<DimensionUserEntity, String> {

    @Autowired
    private ReactiveRepository<DimensionUserEntity, String> repository;

    @Override
    public ReactiveRepository<DimensionUserEntity, String> getRepository() {
        return repository;
    }
}
