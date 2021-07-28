package org.hswebframework.web.crud.web;

import io.swagger.v3.oas.annotations.Operation;
import org.hswebframework.ezorm.rdb.mapping.SyncRepository;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.api.crud.entity.RecordModifierEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.SaveAction;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface SaveController<E, K> {

    @Authorize(ignore = true)
    SyncRepository<E, K> getRepository();

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
    default SaveResult save(@RequestBody List<E> payload) {
        return getRepository()
                .save(Authentication
                              .current()
                              .map(auth -> {
                                  for (E e : payload) {
                                      applyAuthentication(e, auth);
                                  }
                                  return payload;
                              })
                              .orElse(payload)
                );
    }

    @PostMapping("/_batch")
    @SaveAction
    @Operation(summary = "批量新增数据")
    default int add(@RequestBody List<E> payload) {
        return getRepository()
                .insertBatch(Authentication
                                     .current()
                                     .map(auth -> {
                                         for (E e : payload) {
                                             applyAuthentication(e, auth);
                                         }
                                         return payload;
                                     })
                                     .orElse(payload)
                );
    }

    @PostMapping
    @SaveAction
    @Operation(summary = "新增单个数据,并返回新增后的数据.")
    default E add(@RequestBody E payload) {
        this.getRepository()
            .insert(Authentication
                            .current()
                            .map(auth -> applyAuthentication(payload, auth))
                            .orElse(payload));
        return payload;
    }


    @PutMapping("/{id}")
    @SaveAction
    @Operation(summary = "根据ID修改数据")
    default boolean update(@PathVariable K id, @RequestBody E payload) {

        return getRepository()
                .updateById(id, Authentication
                        .current()
                        .map(auth -> applyAuthentication(payload, auth))
                        .orElse(payload))
                > 0;

    }
}
