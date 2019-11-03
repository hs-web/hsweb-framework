package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

public class DefaultPermissionService extends GenericReactiveCrudService<PermissionEntity, String> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public Mono<SaveResult> save(Publisher<PermissionEntity> entityPublisher) {
        return super.save(entityPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of()));
    }

    @Override
    public Mono<Integer> updateById(String id, Mono<PermissionEntity> entityPublisher) {
        return super.updateById(id, entityPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of()));
    }

    @Override
    public Mono<Integer> deleteById(Publisher<String> idPublisher) {
        return super.deleteById(idPublisher)
                .doOnSuccess(r -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of()));
    }

    @Override
    public ReactiveDelete createDelete() {
        return super.createDelete()
                .onExecute(i -> i.doOnSuccess(r -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of())));
    }

    @Override
    public ReactiveUpdate<PermissionEntity> createUpdate() {
        return super.createUpdate()
                .onExecute(i -> i.doOnSuccess(r -> eventPublisher.publishEvent(ClearUserAuthorizationCacheEvent.of())));
    }


}
