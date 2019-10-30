package org.hswebframework.web.crud.service;

import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveQuery;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.PagerResult;
import org.reactivestreams.Publisher;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;

public interface ReactiveCrudService<E, K> {

    ReactiveRepository<E, K> getRepository();

    default ReactiveQuery<E> createQuery() {
        return getRepository().createQuery();
    }

    default ReactiveUpdate<E> createUpdate() {
        return getRepository().createUpdate();
    }

    default ReactiveDelete createDelete() {
        return getRepository().createDelete();
    }

    @Transactional(readOnly = true)
    default Mono<E> findById(Mono<K> publisher) {
        return getRepository()
                .findById(publisher);
    }

    @Transactional(readOnly = true)
    default Flux<E> findById(Flux<K> publisher) {
        return getRepository()
                .findById(publisher);
    }

    @Transactional
    default Mono<SaveResult> save(Publisher<E> entityPublisher) {
        return getRepository()
                .save(entityPublisher);
    }

    @Transactional
    default Mono<Integer> updateById(K id, Mono<E> entityPublisher) {
        return getRepository()
                .updateById(id, entityPublisher);
    }

    @Transactional
    default Mono<Integer> insertBatch(Publisher<? extends Collection<E>> entityPublisher) {
        return getRepository()
                .insertBatch(entityPublisher);
    }

    @Transactional
    default Mono<Integer> insert(Publisher<E> entityPublisher) {
        return getRepository()
                .insert(entityPublisher);
    }

    @Transactional
    default Mono<Integer> deleteById(Publisher<K> idPublisher) {
        return getRepository()
                .deleteById(idPublisher);
    }

    @Transactional(readOnly = true)
    default Flux<E> query(Mono<? extends QueryParam> queryParamMono) {
        return queryParamMono
                .flatMapMany(param -> getRepository()
                        .createQuery()
                        .setParam(param)
                        .fetch());
    }

    @Transactional(readOnly = true)
    default Mono<PagerResult<E>> queryPager(Mono<? extends QueryParam> queryParamMono) {
        return queryParamMono
                .cast(QueryParam.class)
                .flatMap(param -> getRepository()
                        .createQuery()
                        .setParam(param)
                        .count()
                        .flatMap(total -> {
                            if (total == 0) {
                                return Mono.just(PagerResult.empty());
                            }
                            return queryParamMono
                                    .map(QueryParam::clone)
                                    .flatMap(q -> query(Mono.just(q.rePaging(total))).collectList())
                                    .map(list -> PagerResult.of(total, list, param));
                        }));
    }

    @Transactional(readOnly = true)
    default Mono<Integer> count(Mono<? extends QueryParam> queryParamMono) {
        return queryParamMono
                .flatMap(param -> getRepository()
                        .createQuery()
                        .setParam(param)
                        .count());
    }


}
