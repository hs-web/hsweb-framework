package org.hswebframework.web.crud.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.hswebframework.web.api.crud.entity.QueryOperation;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.QueryAction;
import org.hswebframework.web.crud.service.ReactiveTreeSortEntityService;
import org.hswebframework.web.crud.service.TreeSortEntityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface TreeServiceQueryController<E extends TreeSortSupportEntity<K>, K> {

    @Authorize(ignore = true)
    TreeSortEntityService<E, K> getService();

    @GetMapping("/_query/tree")
    @QueryAction
    @QueryOperation(summary = "使用GET动态查询并返回树形结构")
    default  List<E> findAllTree(@Parameter(hidden = true) QueryParamEntity param) {
        return getService().queryResultToTree(param);
    }

    @GetMapping("/_query/_children")
    @QueryAction
    @QueryOperation(summary = "使用GET动态查询并返回子节点数据")
    default List<E> findAllChildren(@Parameter(hidden = true) QueryParamEntity param) {
        return getService().queryIncludeChildren(param);
    }

    @GetMapping("/_query/_children/tree")
    @QueryAction
    @QueryOperation(summary = "使用GET动态查询并返回子节点树形结构数据")
    default List<E> findAllChildrenTree(@Parameter(hidden = true) QueryParamEntity param) {
        return getService().queryIncludeChildrenTree(param);
    }

    @PostMapping("/_query/tree")
    @QueryAction
    @Operation(summary = "使用POST动态查询并返回树形结构")
    default List<E> findAllTreePost(@RequestBody QueryParamEntity param) {
        return getService().queryResultToTree(param);
    }

    @PostMapping("/_query/_children")
    @QueryAction
    @Operation(summary = "使用POST动态查询并返回子节点数据")
    default List<E> findAllChildrenPost(@RequestBody QueryParamEntity param) {
        return  getService().queryIncludeChildren(param);
    }

    @PostMapping("/_query/_children/tree")
    @QueryAction
    @Operation(summary = "使用POST动态查询并返回子节点树形结构数据")
    default List<E> findAllChildrenTreePost(@RequestBody QueryParamEntity param) {
        return getService().queryIncludeChildrenTree(param);
    }

}
