package org.hswebframework.web.crud.web.reactive;

import io.swagger.v3.oas.annotations.Parameter;
import org.hswebframework.web.api.crud.entity.QueryOperation;
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
    @QueryOperation(summary = "使用GET动态查询并返回树形结构")
    default Mono<List<E>> findAllTree(@Parameter(hidden = true) QueryParamEntity paramEntity) {
        return getService().queryResultToTree(paramEntity);
    }

    @GetMapping("/_query/_children")
    @QueryAction
    @QueryOperation(summary = "使用GET动态查询并返回子节点数据")
    default Flux<E> findAllChildren(@Parameter(hidden = true) QueryParamEntity paramEntity) {
        return getService().queryIncludeChildren(paramEntity);
    }

    @GetMapping("/_query/_children/tree")
    @QueryAction
    @QueryOperation(summary = "使用GET动态查询并返回子节点树形结构数据")
    default Mono<List<E>> findAllChildrenTree(@Parameter(hidden = true) QueryParamEntity paramEntity) {
        return getService().queryIncludeChildrenTree(paramEntity);
    }

    @PostMapping("/_query/tree")
    @QueryAction
    @QueryOperation(summary = "使用POST动态查询并返回树形结构")
    default Mono<List<E>> findAllTree(@Parameter(hidden = true) Mono<QueryParamEntity> paramEntity) {
        return getService().queryResultToTree(paramEntity);
    }

    @PostMapping("/_query/_children")
    @QueryAction
    @QueryOperation(summary = "使用POST动态查询并返回子节点数据")
    default Flux<E> findAllChildren(@Parameter(hidden = true) Mono<QueryParamEntity> paramEntity) {
        return paramEntity.flatMapMany(param -> getService().queryIncludeChildren(param));
    }

    @PostMapping("/_query/_children/tree")
    @QueryAction
    @QueryOperation(summary = "使用POST动态查询并返回子节点树形结构数据")
    default Mono<List<E>> findAllChildrenTree(@Parameter(hidden = true) Mono<QueryParamEntity> paramEntity) {
        return paramEntity.flatMap(param -> getService().queryIncludeChildrenTree(param));
    }

}
