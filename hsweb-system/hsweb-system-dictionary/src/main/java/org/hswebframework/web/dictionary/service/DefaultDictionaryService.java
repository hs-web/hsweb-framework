package org.hswebframework.web.dictionary.service;

import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.crud.query.QueryHelper;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.dictionary.entity.DictionaryEntity;
import org.hswebframework.web.dictionary.entity.DictionaryItemEntity;
import org.hswebframework.web.dictionary.event.ClearDictionaryCacheEvent;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public class DefaultDictionaryService extends GenericReactiveCrudService<DictionaryEntity, String> {

    @Autowired
    private DefaultDictionaryItemService itemService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Mono<Integer> insert(Publisher<DictionaryEntity> entityPublisher) {
        return super.insert(entityPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<Integer> insertBatch(Publisher<? extends Collection<DictionaryEntity>> entityPublisher) {
        return super.insertBatch(entityPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<Integer> updateById(String id, Mono<DictionaryEntity> entityPublisher) {
        return super.updateById(id, entityPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<Integer> deleteById(Publisher<String> idPublisher) {
        return super.deleteById(idPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<SaveResult> save(Publisher<DictionaryEntity> entityPublisher) {
        return filterData(entityPublisher)
                .as(super::save)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public ReactiveUpdate<DictionaryEntity> createUpdate() {
        return super.createUpdate()
                .onExecute((ignore, r) -> r.doOnSuccess(l -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of())));
    }

    @Override
    public ReactiveDelete createDelete() {
        return super.createDelete()
                .onExecute((ignore, r) -> r.doOnSuccess(l -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of())));
    }


    public Mono<DictionaryEntity> findDetailById(String id) {
        return findById(Mono.just(id))
                .zipWith(itemService
                                .createQuery()
                                .where(DictionaryItemEntity::getDictId, id)
                                .orderBy(SortOrder.asc(DictionaryItemEntity::getOrdinal))
                                .fetch()
                                .collectList(),
                        (dic, items) -> {
                            dic.setItems(items);
                            return dic;
                        });
    }

    public Flux<DictionaryEntity> findAllDetail(QueryParamEntity paramEntity, boolean allowEmptyItem) {
        return createQuery()
                .setParam(paramEntity)
                .fetch()
                .as(flux -> fillDetail(flux, allowEmptyItem));
    }

    /**
     * 查询字典详情
     *
     * @param dictionary     源数据
     * @param allowEmptyItem 是否允许item为空
     */
    public Flux<DictionaryEntity> fillDetail(Flux<DictionaryEntity> dictionary, boolean allowEmptyItem) {
        return QueryHelper
                .combineOneToMany(
                        dictionary,
                        DictionaryEntity::getId,
                        itemService.createQuery(),
                        DictionaryItemEntity::getDictId,
                        DictionaryEntity::setItems
                )
                //根据条件过滤是否允许返回item为空的
                .filter(dict -> allowEmptyItem || CollectionUtils.isNotEmpty(dict.getItems()));
    }

    public Flux<DictionaryEntity> filterData(Publisher<DictionaryEntity> entityPublisher) {
        Flux<DictionaryEntity> dictCache = Flux.concat(entityPublisher).cache();
        return Mono.zip(dictCache
                                .map(DictionaryEntity::getId)
                                .collectList(),
                        this.createQuery()
                                .and(DictionaryEntity::getClassified, "system")
                                .fetch()
                                .map(DictionaryEntity::getId)
                                .collectList()
                ).
                flatMapMany(tp2 -> {
                    //移出是system的数据
                    List<String> t1 = tp2.getT1();
                    t1.removeAll(tp2.getT2());
                    if (t1.isEmpty()) {
                        return Flux.empty();
                    }
                    return dictCache
                            .filter(entity -> t1.contains(entity.getId()));
                });
    }
}
