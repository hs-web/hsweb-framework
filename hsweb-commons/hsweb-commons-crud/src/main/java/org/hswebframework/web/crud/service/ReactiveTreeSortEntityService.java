package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.TreeSortSupportEntity;
import org.hswebframework.web.api.crud.entity.TreeSupportEntity;
import org.hswebframework.web.id.IDGenerator;
import org.reactivestreams.Publisher;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

public interface ReactiveTreeSortEntityService<E extends TreeSortSupportEntity<K>, K>
        extends ReactiveCrudService<E, K> {

    default Mono<List<E>> queryResultToTree(Mono<? extends QueryParam> paramEntity) {
        return query(paramEntity)
                .collectList()
                .map(list -> TreeSupportEntity.list2tree(list, this::setChildren, this::isRootNode));
    }

    @Override
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return insertBatch(Flux.from(entityPublisher).collectList());
    }

    @Override
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        return this.getRepository()
                .insertBatch(Flux.from(entityPublisher)
                .flatMap(Flux::fromIterable)
                .flatMap(e -> Flux.fromIterable(TreeSupportEntity.expandTree2List(e, getIDGenerator())))
                .collectList());
    }

    @Override
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        return this.getRepository()
                .save(Flux.from(entityPublisher)
                        //把树结构平铺
                        .flatMap(e -> Flux.fromIterable(TreeSupportEntity.expandTree2List(e, getIDGenerator()))));
    }

    @Override
    default Mono<Integer> updateById(K id, Mono<E> entityPublisher) {
        return save(entityPublisher
                .map(e -> {
                    e.setId(id);
                    return e;
                })).map(SaveResult::getTotal);
    }

    @Override
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        return findById(Flux.from(idPublisher))
                .flatMap(e -> createDelete()
                        .where()
                        .like$(e::getPath)
                        .execute())
                .collect(Collectors.summingInt(Integer::intValue));
    }

    IDGenerator<K> getIDGenerator();

    void setChildren(E entity, List<E> children);

    List<E> getChildren(E entity);

   default boolean isRootNode(E entity){
       return StringUtils.isEmpty(entity.getParentId()) || "-1".equals(String.valueOf(entity.getParentId()));
   }
}
