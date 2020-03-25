package org.hswebframework.web.crud.web.reactive;

import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.crud.service.ReactiveTreeSortEntityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ReactiveTreeServiceQueryController<E extends TreeSortSupportEntity<K>, K> {

    @Authorize(ignore = true)
    ReactiveTreeSortEntityService<E, K> getService();

    @GetMapping("/_query/tree")
    @QueryAction
    default Mono<List<E>> findAllTree(QueryParamEntity paramEntity) {
        return getService().queryResultToTree(paramEntity);
    }

    @GetMapping("/_query/_children")
    @QueryAction
    default Flux<E> findAllChildren(QueryParamEntity paramEntity) {
        return getService().queryIncludeChildren(paramEntity);
    }

    @GetMapping("/_query/_children/tree")
    @QueryAction
    default Mono<List<E>> findAllChildrenTree(QueryParamEntity paramEntity) {
        return getService().queryIncludeChildrenTree(paramEntity);
    }

    @PostMapping("/_query/tree")
    @QueryAction
    default Mono<List<E>> findAllTree(Mono<QueryParamEntity> paramEntity) {
        return getService().queryResultToTree(paramEntity);
    }

    @PostMapping("/_query/_children")
    @QueryAction
    default Flux<E> findAllChildren(Mono<QueryParamEntity> paramEntity) {
        return paramEntity.flatMapMany(param -> getService().queryIncludeChildren(param));
    }

    @PostMapping("/_query/_children/tree")
    @QueryAction
    default Mono<List<E>> findAllChildrenTree(Mono<QueryParamEntity> paramEntity) {
        return paramEntity.flatMap(param -> getService().queryIncludeChildrenTree(param));
    }

}
