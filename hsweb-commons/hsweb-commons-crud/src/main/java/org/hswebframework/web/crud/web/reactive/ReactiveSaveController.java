package org.hswebframework.web.crud.web.reactive;

import io.swagger.v3.oas.annotations.Operation;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.api.crud.entity.RecordModifierEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

public interface ReactiveSaveController<E, K> {

    @Authorize(ignore = true)
    ReactiveRepository<E, K> getRepository();

    @Authorize(ignore = true)
    default E applyCreationEntity(Authentication authentication, E entity) {
        RecordCreationEntity creationEntity = ((RecordCreationEntity) entity);
        creationEntity.setCreateTimeNow();
        creationEntity.setCreatorId(authentication.getUser().getId());
        creationEntity.setCreatorName(authentication.getUser().getName());
        return entity;
    }

    @Authorize(ignore = true)
    default E applyModifierEntity(Authentication authentication, E entity) {
        RecordModifierEntity modifierEntity = ((RecordModifierEntity) entity);
        modifierEntity.setModifyTimeNow();
        modifierEntity.setModifierId(authentication.getUser().getId());
        modifierEntity.setModifierName(authentication.getUser().getName());
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
    @Operation(summary = "保存数据", description = "如果传入了id,并且对应数据存在,则尝试覆盖,不存在则新增.")
    default Mono<SaveResult> save(@RequestBody Flux<E> payload) {
        return Authentication.currentReactive()
                .flatMapMany(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .as(getRepository()::save);
    }

    @PostMapping("/_batch")
    @SaveAction
    @Operation(summary = "批量新增数据")
    default Mono<Integer> add(@RequestBody Flux<E> payload) {

        return Authentication.currentReactive()
                .flatMapMany(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .collectList()
                .as(getRepository()::insertBatch);
    }

    @PostMapping
    @SaveAction
    @Operation(summary = "新增单个数据,并返回新增后的数据.")
    default Mono<E> add(@RequestBody Mono<E> payload) {
        return Authentication.currentReactive()
                .flatMap(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .flatMap(entity -> getRepository().insert(Mono.just(entity)).thenReturn(entity));
    }


    @PutMapping("/{id}")
    @SaveAction
    @Operation(summary = "根据ID修改数据")
    default Mono<Boolean> update(@PathVariable K id, @RequestBody Mono<E> payload) {

        return Authentication.currentReactive()
                .flatMap(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .flatMap(entity -> getRepository().updateById(id, Mono.just(entity)))
                .thenReturn(true);

    }
}
