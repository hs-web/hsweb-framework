package org.hswebframework.web.dictionary.service;

import org.apache.commons.collections4.MapUtils;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.api.crud.entity.SortSupportEntity;
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
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultDictionaryService extends GenericReactiveCrudService<DictionaryEntity, String> {

    @Autowired
    private DefaultDictionaryItemService itemService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Mono<Integer> insert(Publisher<DictionaryEntity> entityPublisher) {
        return super.insert(entityPublisher)
                .doOnSuccess(r->eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<Integer> insertBatch(Publisher<? extends Collection<DictionaryEntity>> entityPublisher) {
        return super.insertBatch(entityPublisher)
                .doOnSuccess(r->eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<Integer> updateById(String id, Mono<DictionaryEntity> entityPublisher) {
        return super.updateById(id,entityPublisher)
                .doOnSuccess(r->eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<Integer> deleteById(Publisher<String> idPublisher) {
        return super.deleteById(idPublisher)
                .doOnSuccess(r->eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<SaveResult> save(Publisher<DictionaryEntity> entityPublisher) {
        return super.save(entityPublisher)
                .doOnSuccess(r->eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public ReactiveUpdate<DictionaryEntity> createUpdate() {
        return super.createUpdate()
                .onExecute((ignore,r)->r.doOnSuccess(l->eventPublisher.publishEvent(ClearDictionaryCacheEvent.of())));
    }

    @Override
    public ReactiveDelete createDelete() {
        return super.createDelete()
                .onExecute((ignore,r)->r.doOnSuccess(l->eventPublisher.publishEvent(ClearDictionaryCacheEvent.of())));
    }


    public Mono<DictionaryEntity> findDetailById(String id) {
        return findById(Mono.just(id))
                .zipWith(itemService
                                .createQuery()
                                .where(DictionaryItemEntity::getDictId, id)
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
        return dictionary
                .collect(Collectors.toMap(DictionaryEntity::getId, Function.identity()))
                .flatMapMany(dicMap -> {
                    if (MapUtils.isEmpty(dicMap)) {
                        return Mono.empty();
                    }
                    return itemService
                            .createQuery()
                            .in(DictionaryItemEntity::getDictId, dicMap.keySet())
                            .fetch()
                            .groupBy(DictionaryItemEntity::getDictId)
                            .flatMap(group -> Mono
                                    .justOrEmpty(dicMap.remove(group.key()))
                                    .zipWhen(dict -> group.collectList(),
                                             (dict, items) -> {
                                                 items.sort(SortSupportEntity::compareTo);
                                                 dict.setItems(items);
                                                 return dict;
                                             }))
                            .concatWith(Flux.defer(() -> {
                                if (allowEmptyItem) {
                                    //允许返回item为空的数据字典
                                    return Flux.fromIterable(dicMap.values());
                                }
                                return Mono.empty();
                            }));
                });
    }


}
