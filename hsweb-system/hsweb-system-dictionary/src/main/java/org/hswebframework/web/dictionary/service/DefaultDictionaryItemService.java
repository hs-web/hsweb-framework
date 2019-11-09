package org.hswebframework.web.dictionary.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.crud.service.ReactiveTreeSortEntityService;
import org.hswebframework.web.dictionary.entity.DictionaryItemEntity;
import org.hswebframework.web.dictionary.event.ClearDictionaryCacheEvent;
import org.hswebframework.web.id.IDGenerator;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

public class DefaultDictionaryItemService extends GenericReactiveCrudService<DictionaryItemEntity, String>
        implements ReactiveTreeSortEntityService<DictionaryItemEntity, String> {

    @Autowired
    public ApplicationEventPublisher eventPublisher;

    @Override
    public IDGenerator<String> getIDGenerator() {
        return IDGenerator.SNOW_FLAKE_STRING;
    }

    @Override
    public void setChildren(DictionaryItemEntity entity, List<DictionaryItemEntity> children) {
        entity.setChildren(children);
    }

    @Override
    public Mono<Integer> insert(Publisher<DictionaryItemEntity> entityPublisher) {
        return super.insert(entityPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<Integer> insertBatch(Publisher<? extends Collection<DictionaryItemEntity>> entityPublisher) {
        return super.insertBatch(entityPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<Integer> updateById(String id, Mono<DictionaryItemEntity> entityPublisher) {
        return super.updateById(id, entityPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<Integer> deleteById(Publisher<String> idPublisher) {
        return super.deleteById(idPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<SaveResult> save(Publisher<DictionaryItemEntity> entityPublisher) {
        return super.save(entityPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public ReactiveUpdate<DictionaryItemEntity> createUpdate() {
        return super.createUpdate()
                .onExecute((ignore, r) -> r.doOnSuccess(l -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of())));
    }

    @Override
    public ReactiveDelete createDelete() {
        return super.createDelete()
                .onExecute((ignore, r) -> r.doOnSuccess(l -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of())));
    }
}
