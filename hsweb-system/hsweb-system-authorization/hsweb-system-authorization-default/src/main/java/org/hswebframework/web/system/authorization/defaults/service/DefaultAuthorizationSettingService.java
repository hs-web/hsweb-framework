package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.authorization.DimensionProvider;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultAuthorizationSettingService extends GenericReactiveCrudService<AuthorizationSettingEntity, String> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private List<DimensionProvider> providers;


    @Override
    public Mono<SaveResult> save(Publisher<AuthorizationSettingEntity> entityPublisher) {
        return Flux.from(entityPublisher)
                .collectList()
                .flatMap(autz -> super.save(Flux.fromIterable(autz)).doOnSuccess(r -> clearUserAuthCache(autz)));
    }

    @Override
    public Mono<Integer> updateById(String id, Mono<AuthorizationSettingEntity> entityPublisher) {
        return entityPublisher
                .flatMap(autz -> super.updateById(id, Mono.just(autz))
                        .doOnSuccess((r) -> clearUserAuthCache(Collections.singletonList(autz))));
    }

    @Override
    public Mono<Integer> deleteById(Publisher<String> idPublisher) {
        return Flux.from(idPublisher)
                .collectList()
                .flatMap(list -> super.deleteById(Flux.fromIterable(list))
                        .flatMap(r -> findById(Flux.fromIterable(list))
                                .collectList()
                                .doOnSuccess(this::clearUserAuthCache)
                                .thenReturn(r)));
    }

    @Override
    public Mono<Integer> insert(Publisher<AuthorizationSettingEntity> entityPublisher) {

        return Flux.from(entityPublisher)
                .collectList()
                .flatMap(list -> super.insert(Flux.fromIterable(list))
                        .doOnSuccess(i -> clearUserAuthCache(list)));
    }

    @Override
    public Mono<Integer> insertBatch(Publisher<? extends Collection<AuthorizationSettingEntity>> entityPublisher) {
        return Flux.from(entityPublisher)
                .collectList()
                .flatMap(list -> super.insertBatch(Flux.fromIterable(list))
                        .doOnSuccess(i -> clearUserAuthCache(list.stream().flatMap(Collection::stream).collect(Collectors.toList()))));
    }

    @Override
    public ReactiveUpdate<AuthorizationSettingEntity> createUpdate() {
        ReactiveUpdate<AuthorizationSettingEntity> update = super.createUpdate();

        return update.onExecute(r ->
                r.doOnSuccess(i -> {
                    createQuery()
                            .setParam(update.toQueryParam())
                            .fetch()
                            .collectList()
                            .subscribe(this::clearUserAuthCache);
                }));
    }

    @Override
    public ReactiveDelete createDelete() {
        ReactiveDelete delete = super.createDelete();
        return delete.onExecute(r ->
                r.doOnSuccess(i -> {
                    createQuery()
                            .setParam(delete.toQueryParam())
                            .fetch()
                            .collectList()
                            .subscribe(this::clearUserAuthCache);
                }));
    }

    protected void clearUserAuthCache(List<AuthorizationSettingEntity> settings) {
        Flux.fromIterable(providers)
                .flatMap(provider ->
                        //按维度类型进行映射
                        provider.getAllType()
                                .map(DimensionType::getId)
                                .map(t -> Tuples.of(t, provider)))
                .collect(Collectors.toMap(Tuple2::getT1, Tuple2::getT2))
                .flatMapMany(typeProviderMapping -> Flux
                        .fromIterable(settings)//根据维度获取所有userId
                        .flatMap(setting -> Mono.justOrEmpty(typeProviderMapping.get(setting.getDimensionType()))
                                .flatMapMany(provider -> provider.getUserIdByDimensionId(setting.getDimensionTarget()))))
                .collectList()
                .map(ClearUserAuthorizationCacheEvent::of)
                .subscribe(eventPublisher::publishEvent);
    }
}
