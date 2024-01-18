package org.hswebframework.web.dictionary.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.crud.service.ReactiveTreeSortEntityService;
import org.hswebframework.web.dictionary.entity.DictionaryItemEntity;
import org.hswebframework.web.dictionary.event.ClearDictionaryCacheEvent;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.id.IDGenerator;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
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
        return super.insert(this.fillOrdinal(entityPublisher))
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearDictionaryCacheEvent.of()));
    }

    @Override
    public Mono<Integer> insertBatch(Publisher<? extends Collection<DictionaryItemEntity>> entityPublisher) {
        return super.insertBatch(fillCollectionOrdinal(entityPublisher))
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
        return super.save(this.fillOrdinal(entityPublisher))
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

    public Publisher<? extends Collection<DictionaryItemEntity>> fillCollectionOrdinal(Publisher<? extends Collection<DictionaryItemEntity>> entityPublisher){
        return Flux
                .from(entityPublisher)
                .flatMap(collection-> fillOrdinal(Flux.fromIterable(collection)).collectList());
    }


    public Flux<DictionaryItemEntity> fillOrdinal(Publisher<DictionaryItemEntity> publisher) {
        return Flux
                .from(publisher)
                .groupBy(DictionaryItemEntity::getDictId)
                .flatMap(group -> group
                        .collectList()
                        .flatMapMany(list -> {
                            boolean isNull = list.stream().allMatch(item -> item.getOrdinal() == null);
                            boolean notNull = list.stream().allMatch(item -> item.getOrdinal() != null);
                            if (notNull) {
                                return Flux.fromIterable(list);
                            }
                            if (isNull) {
                                return this
                                        .createQuery()
                                        .select(DictionaryItemEntity::getOrdinal)
                                        .where(DictionaryItemEntity::getDictId, group.key())
                                        .orderBy(SortOrder.desc(DictionaryItemEntity::getOrdinal))
                                        .fetchOne()
                                        .map(DictionaryItemEntity::getOrdinal)
                                        .defaultIfEmpty(-1)
                                        .flatMapMany(maxOrdinal -> Flux
                                                .fromIterable(list)
                                                .index()
                                                .map(tp2 -> {
                                                    DictionaryItemEntity t2 = tp2.getT2();
                                                    int ordinal = tp2.getT1().intValue() + maxOrdinal + 1;
                                                    t2.setOrdinal(ordinal);
                                                    return t2;
                                                }));
                            }
                            return Mono.error(() -> new BusinessException("error.ordinal_can_not_null"));

                        }));
    }


}
