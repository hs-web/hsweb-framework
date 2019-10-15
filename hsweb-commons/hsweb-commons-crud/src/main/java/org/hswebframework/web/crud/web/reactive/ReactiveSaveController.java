package org.hswebframework.web.crud.web.reactive;

import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.api.crud.entity.RecordModifierEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

public interface ReactiveSaveController<E, K> {

    ReactiveRepository<E, K> getRepository();

    default E applyCreationEntity(Authentication authentication, E entity) {
        RecordCreationEntity creationEntity = ((RecordCreationEntity) entity);
        creationEntity.setCreateTimeNow();
        creationEntity.setCreatorId(authentication.getUser().getId());

        return entity;
    }

    default E applyModifierEntity(Authentication authentication, E entity) {
        RecordModifierEntity creationEntity = ((RecordModifierEntity) entity);
        creationEntity.setModifyTimeNow();
        creationEntity.setModifierId(authentication.getUser().getId());

        return entity;
    }

    default E applyAuthentication(Authentication authentication, E entity) {
        if (entity instanceof RecordCreationEntity) {
            entity = applyCreationEntity(authentication, entity);
        }
        if (entity instanceof RecordModifierEntity) {
            entity = applyModifierEntity(authentication, entity);
        }
        return entity;
    }

    @PatchMapping
    @Authorize(action = Permission.ACTION_UPDATE)
    default Mono<E> save(@RequestBody Mono<E> payload) {
        return Authentication.currentReactive()
                .zipWith(payload, this::applyAuthentication)
                .switchIfEmpty(payload)
                .flatMap(entity -> getRepository().save(Mono.just(entity)).thenReturn(entity));
    }

    @PostMapping
    @Authorize(action = Permission.ACTION_UPDATE)
    default Mono<E> add(@RequestBody Mono<E> payload) {
        return  Authentication.currentReactive()
                .zipWith(payload, this::applyAuthentication)
                .switchIfEmpty(payload)
                .flatMap(entity -> getRepository().insert(Mono.just(entity)).thenReturn(entity));
    }

    @PutMapping("/{id}")
    @Authorize(action = Permission.ACTION_UPDATE)
    default Mono<E> update(@PathVariable K id, @RequestBody Mono<E> payload) {
        return  Authentication.currentReactive()
                .zipWith(payload, this::applyAuthentication)
                .switchIfEmpty(payload)
                .flatMap(entity -> getRepository().updateById(id,Mono.just(entity)).thenReturn(entity));
    }
}
