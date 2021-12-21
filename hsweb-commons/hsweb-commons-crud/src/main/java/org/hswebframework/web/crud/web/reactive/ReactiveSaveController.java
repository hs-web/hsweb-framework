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

/**
 * 响应式保存接口,基于{@link  ReactiveRepository}提供默认的新增,保存,修改接口.
 *
 * @param <E> 实体类型
 * @param <K> 主键类型
 */
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

    /**
     * 尝试设置登陆用户信息到实体中
     *
     * @param entity         实体
     * @param authentication 权限信息
     * @see RecordCreationEntity
     * @see RecordModifierEntity
     */
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

    /**
     * 保存数据,如果传入了id,并且对应数据存在,则尝试覆盖,不存在则新增.
     * <br><br>
     * 以类注解{@code @RequestMapping("/api/test")}为例:
     * <pre>{@code
     *
     * PATCH /api/test
     * Content-Type: application/json
     *
     * [
     *  {
     *   "name":"value"
     *  }
     * ]
     * }
     * </pre>
     *
     * @param payload payload
     * @return 保存结果
     */
    @PatchMapping
    @SaveAction
    @Operation(summary = "保存数据", description = "如果传入了id,并且对应数据存在,则尝试覆盖,不存在则新增.")
    default Mono<SaveResult> save(@RequestBody Flux<E> payload) {
        return Authentication
                .currentReactive()
                .flatMapMany(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .as(getRepository()::save);
    }

    /**
     * 批量新增
     * <br><br>
     * 以类注解{@code @RequestMapping("/api/test")}为例:
     * <pre>{@code
     *
     * POST /api/test/_batch
     * Content-Type: application/json
     *
     * [
     *  {
     *   "name":"value"
     *  }
     * ]
     * }
     * </pre>
     *
     * @param payload payload
     * @return 保存结果
     */
    @PostMapping("/_batch")
    @SaveAction
    @Operation(summary = "批量新增数据")
    default Mono<Integer> add(@RequestBody Flux<E> payload) {
        return Authentication
                .currentReactive()
                .flatMapMany(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .collectList()
                .as(getRepository()::insertBatch);
    }

    /**
     * 新增单个数据,并返回新增后的数据.
     * <br><br>
     * 以类注解{@code @RequestMapping("/api/test")}为例:
     * <pre>{@code
     *
     * POST /api/test
     * Content-Type: application/json
     *
     *  {
     *   "name":"value"
     *  }
     * }
     * </pre>
     *
     * @param payload payload
     * @return 新增后的数据
     */
    @PostMapping
    @SaveAction
    @Operation(summary = "新增单个数据,并返回新增后的数据.")
    default Mono<E> add(@RequestBody Mono<E> payload) {
        return Authentication
                .currentReactive()
                .flatMap(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .flatMap(entity -> getRepository().insert(Mono.just(entity)).thenReturn(entity));
    }


    /**
     * 根据ID修改数据
     * <br><br>
     * 以类注解{@code @RequestMapping("/api/test")}为例:
     * <pre>{@code
     *
     * PUT /api/test/{id}
     * Content-Type: application/json
     *
     *  {
     *   "name":"value"
     *  }
     * }
     * </pre>
     *
     * @param payload payload
     * @return 是否成功
     */
    @PutMapping("/{id}")
    @SaveAction
    @Operation(summary = "根据ID修改数据")
    default Mono<Boolean> update(@PathVariable K id, @RequestBody Mono<E> payload) {
        return Authentication
                .currentReactive()
                .flatMap(auth -> payload.map(entity -> applyAuthentication(entity, auth)))
                .switchIfEmpty(payload)
                .flatMap(entity -> getRepository().updateById(id, Mono.just(entity)))
                .thenReturn(true);

    }
}
