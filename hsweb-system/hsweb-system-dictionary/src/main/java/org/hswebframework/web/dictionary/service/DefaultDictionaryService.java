package org.hswebframework.web.dictionary.service;

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

    public Flux<DictionaryEntity> findAllDetail(QueryParamEntity paramEntity) {
        /*
            1. 查询出所有字典并以ID为key转为map
            2. 查询出所有字段选项并按dicId分组
            3. 根据分组后的key(dictId)获取字段
            4. 将2的分组结果放到字典里
         */
        return createQuery()
                .setParam(paramEntity)
                .fetch()
                .collect(Collectors.toMap(DictionaryEntity::getId, Function.identity())) //.1
                .flatMapMany(dicMap ->
                        itemService.createQuery()
                                .fetch()
                                .groupBy(DictionaryItemEntity::getDictId)//.2
                                .flatMap(group -> Mono
                                        .justOrEmpty(dicMap.get(group.key())) //.3
                                        .zipWhen(dict -> group.collectList(),
                                                (dict, items) -> {
                                                    items.sort(SortSupportEntity::compareTo);
                                                    dict.setItems(items);  //.4
                                                    return dict;
                                                })));
    }

}
