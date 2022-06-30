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
                    .flatMap(e -> ClearUserAuthorizationCacheEvent.all().publish(eventPublisher).thenReturn(e));
    }

    @Override
    public Mono<Integer> updateById(String id, Mono<PermissionEntity> entityPublisher) {
        return super.updateById(id, entityPublisher)
                    .flatMap(e -> ClearUserAuthorizationCacheEvent.all().publish(eventPublisher).thenReturn(e));
    }

    @Override
    public Mono<Integer> deleteById(Publisher<String> idPublisher) {
        return super.deleteById(idPublisher)
                    .flatMap(e -> ClearUserAuthorizationCacheEvent.all().publish(eventPublisher).thenReturn(e));
    }

    @Override
    public ReactiveDelete createDelete() {
        return super.createDelete()
                    .onExecute((ignore, i) -> i
                            .flatMap(e -> ClearUserAuthorizationCacheEvent
                                    .all()
                                    .publish(eventPublisher)
                                    .thenReturn(e)));
    }

    @Override
    public ReactiveUpdate<PermissionEntity> createUpdate() {
        return super.createUpdate()
                    .onExecute((ignore, i) -> i
                            .flatMap(e -> ClearUserAuthorizationCacheEvent
                                    .all()
                                    .publish(eventPublisher)
                                    .thenReturn(e)));
    }


}
