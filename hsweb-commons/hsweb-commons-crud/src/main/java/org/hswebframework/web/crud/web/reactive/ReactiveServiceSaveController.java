package org.hswebframework.web.crud.web.reactive;

import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.api.crud.entity.RecordModifierEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.hswebframework.web.crud.service.ReactiveCrudService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ReactiveServiceSaveController<E,K>  {

    @Authorize(ignore = true)
    ReactiveCrudService<E,K> getService();

    @Authorize(ignore = true)
    default E applyCreationEntity(Authentication authentication, E entity) {
        RecordCreationEntity creationEntity = ((RecordCreationEntity) entity);
        creationEntity.setCreateTimeNow();
        creationEntity.setCreatorId(authentication.getUser().getId());

        return entity;
    }

    @Authorize(ignore = true)
    default E applyModifierEntity(Authentication authentication, E entity) {
        RecordModifierEntity creationEntity = ((RecordModifierEntity) entity);
        creationEntity.setModifyTimeNow();
        creationEntity.setModifierId(authentication.getUser().getId());

        return entity;
    }

    @Authorize(ignore = true)
    default E applyAuthentication(E entity, Authentication authentication) {
        if (entity instanceof RecordCreationEntity) {
            entity = applyCreationEntity(authentication, entity);
        }
        if (entity instanceof RecordModifierEntity) {
            entity = applyModifierEntity(authentication, entity);
        }
        return entity;
    }

    @PatchMapping
    @SaveAction
    default Mono<SaveResult> save(@RequestBody Flux<E> payload) {
        return Authentication.currentReactive()
                .flatMapMany(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .as(getService()::save);
    }

    @PostMapping("/_batch")
    @SaveAction
    default Mono<Integer> add(@RequestBody Flux<E> payload) {

        return Authentication.currentReactive()
                .flatMapMany(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .collectList()
                .as(getService()::insertBatch);
    }

    @PostMapping
    @SaveAction
    default Mono<E> add(@RequestBody Mono<E> payload) {
        return Authentication.currentReactive()
                .flatMap(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .flatMap(entity -> getService().insert(Mono.just(entity)).thenReturn(entity));
    }


    @PutMapping("/{id}")
    @SaveAction
    default Mono<Boolean> update(@PathVariable K id, @RequestBody Mono<E> payload) {

        return Authentication.currentReactive()
                .flatMap(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .flatMap(entity -> getService().updateById(id, Mono.just(entity)))
                .thenReturn(true);

    }
}
