package org.hswebframework.web.system.authorization.defaults.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.hswebframework.ezorm.rdb.mapping.ReactiveDelete;
import org.hswebframework.ezorm.rdb.mapping.ReactiveQuery;
import org.hswebframework.ezorm.rdb.mapping.ReactiveUpdate;
import org.hswebframework.ezorm.rdb.mapping.defaults.SaveResult;
import org.hswebframework.web.crud.events.EntityCreatedEvent;
import org.hswebframework.web.crud.events.EntityDeletedEvent;
import org.hswebframework.web.crud.events.EntityModifyEvent;
import org.hswebframework.web.crud.events.EntityPrepareCreateEvent;
import org.hswebframework.web.crud.events.EntityPrepareSaveEvent;
import org.hswebframework.web.crud.events.EntitySavedEvent;
import org.hswebframework.web.crud.service.GenericReactiveCrudService;
import org.hswebframework.web.event.AsyncEvent;
import org.hswebframework.web.exception.BusinessException;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;
import org.hswebframework.web.system.authorization.api.event.ClearUserAuthorizationCacheEvent;
import org.hswebframework.web.system.authorization.api.event.DimensionBindEvent;
import org.hswebframework.web.system.authorization.api.event.DimensionUnbindEvent;
import org.hswebframework.web.system.authorization.api.event.UserDeletedEvent;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.util.ObjectUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.Function3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Slf4j
public class DefaultDimensionUserService extends GenericReactiveCrudService<DimensionUserEntity, String> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @EventListener
    public void prepareMemberIdentity(EntityPrepareCreateEvent<DimensionUserEntity> event) {
        event.async(prepareMemberIdentity(event.getEntity()));
    }

    @EventListener
    public void prepareMemberIdentity(EntityPrepareSaveEvent<DimensionUserEntity> event) {
        event.async(prepareMemberIdentity(event.getEntity()));
    }

    private Mono<Void> prepareMemberIdentity(List<DimensionUserEntity> members) {
        // prepare 早于 SQL 物化；每片一次查询，service 和 repository 共享身份规则。
        Map<String, List<String>> resolvedIds = new HashMap<>();
        return Flux.fromIterable(members).buffer(200)
            .concatMap(batch -> resolveMemberIdentity(batch, resolvedIds)).then();
    }

    private Mono<Void> resolveMemberIdentity(List<DimensionUserEntity> members,
                                              Map<String, List<String>> resolvedIds) {
        Set<String> ids = new LinkedHashSet<>();
        Set<List<String>> keys = new LinkedHashSet<>();
        for (DimensionUserEntity member : members) {
            if (!ObjectUtils.isEmpty(member.getId())) {
                ids.add(member.getId());
            }
            if (hasMemberKey(member)) {
                keys.add(memberKey(member));
                if (ObjectUtils.isEmpty(member.getId())) {
                    // 候选 ID 只用于同一批查询，不能提前成为调用方显式 ID 或抢占旧自然键。
                    DimensionUserEntity candidate = member.copyTo(DimensionUserEntity.class);
                    candidate.generateId();
                    ids.add(candidate.getId());
                }
            }
        }
        if (ids.isEmpty() && keys.isEmpty()) {
            // 不完整的新输入仍由原写入校验及数据库约束处理，不能使用 null 条件匹配其他行。
            return Mono.empty();
        }
        ReactiveQuery<DimensionUserEntity> query = createQuery();
        if (!ids.isEmpty()) {
            query.in(DimensionUserEntity::getId, ids);
        }
        for (List<String> key : keys) {
            query.orNest().is(DimensionUserEntity::getDimensionTypeId, key.get(0))
                .and(DimensionUserEntity::getDimensionId, TermType.eq, key.get(1))
                .and(DimensionUserEntity::getUserId, TermType.eq, key.get(2)).end();
        }
        return query.fetch().collectList().doOnNext(existing -> {
            Map<String, DimensionUserEntity> byId = new HashMap<>();
            Map<List<String>, DimensionUserEntity> byMember = new HashMap<>();
            for (DimensionUserEntity row : existing) {
                byId.put(row.getId(), row);
                byMember.putIfAbsent(memberKey(row), row);
            }
            for (DimensionUserEntity member : members) {
                DimensionUserEntity row = byId.get(member.getId());
                // 先检查传入 ID；稀疏保存只校验已提供的身份列，不回填业务字段。
                if (row != null && !matchesProvidedIdentity(row, member)) {
                    throw new BusinessException("error.dimension_user_id_conflict", 400, member.getId());
                }
                if (row == null && hasMemberKey(member)) {
                    row = byMember.get(memberKey(member));
                }
                if (row != null) {
                    member.setId(row.getId());
                } else if (hasMemberKey(member)) {
                    member.generateId();
                    DimensionUserEntity occupied = byId.get(member.getId());
                    if (occupied != null && !matchesProvidedIdentity(occupied, member)) {
                        throw new BusinessException("error.dimension_user_id_conflict", 400, member.getId());
                    }
                }
                if (row != null || hasMemberKey(member)) {
                    List<String> key = memberKey(row == null ? member : row);
                    List<String> previous = resolvedIds.putIfAbsent(member.getId(), key);
                    // 一次 prepare 全部完成后才写 SQL；跨查询片也不能让不同成员共用公开 ID。
                    if (previous != null && !previous.equals(key)) {
                        throw new BusinessException("error.dimension_user_id_conflict", 400, member.getId());
                    }
                }
            }
        }).then();
    }

    private boolean hasMemberKey(DimensionUserEntity member) {
        return member.getDimensionTypeId() != null && member.getDimensionId() != null && member.getUserId() != null;
    }

    private List<String> memberKey(DimensionUserEntity member) {
        return Arrays.asList(member.getDimensionTypeId(), member.getDimensionId(), member.getUserId());
    }

    private boolean matchesProvidedIdentity(DimensionUserEntity stored, DimensionUserEntity input) {
        return (input.getDimensionTypeId() == null || Objects.equals(stored.getDimensionTypeId(), input.getDimensionTypeId()))
            && (input.getDimensionId() == null || Objects.equals(stored.getDimensionId(), input.getDimensionId()))
            && (input.getUserId() == null || Objects.equals(stored.getUserId(), input.getUserId()));
    }

    //处理用户被删除时,同步删除维度绑定信息
    @EventListener
    public void handleUserDeleteEntity(UserDeletedEvent event) {
        event.async(this.createDelete()
                        .where(DimensionUserEntity::getUserId, event.getUser().getId())
                        .execute()
                        .doOnSuccess(i -> log.debug("user deleted,clear user dimension!"))
        );
    }

    //转发保存维度信息到DimensionBindEvent事件,并清空权限缓存
    @EventListener
    public void dispatchDimensionBind(EntitySavedEvent<DimensionUserEntity> event) {
        event.async(
                this.publishEvent(Flux.fromIterable(event.getEntity()), DimensionBindEvent::new)
        );
    }

    //新增绑定时转发DimensionBindEvent并清空用户权限信息
    @EventListener
    public void dispatchDimensionBind(EntityCreatedEvent<DimensionUserEntity> event) {
        event.async(
                this.publishEvent(Flux.fromIterable(event.getEntity()), DimensionBindEvent::new)
        );
    }

    //删除绑定时转发DimensionUnbindEvent并清空用户权限信息
    @EventListener
    public void dispatchDimensionUnbind(EntityDeletedEvent<DimensionUserEntity> event) {
        event.async(
                this.publishEvent(Flux.fromIterable(event.getEntity()), DimensionUnbindEvent::new)
        );
    }

    //修改绑定信息时清空权限
    @EventListener
    public void handleModifyEvent(EntityModifyEvent<DimensionUserEntity> event) {
        event.async(
                this.clearUserCache(event.getAfter())
        );
    }

    //维度被删除时同时删除绑定信息
    @EventListener
    public void handleDimensionDeletedEntity(EntityDeletedEvent<DimensionEntity> event) {
        event.async(
                Flux.fromIterable(event.getEntity())
                    .collect(groupingBy(DimensionEntity::getTypeId,
                                        mapping(DimensionEntity::getId, toSet())))
                    .flatMapIterable(Map::entrySet)
                    .flatMap(entry -> this
                            .createDelete()
                            .where(DimensionUserEntity::getDimensionTypeId, entry.getKey())
                            .in(DimensionUserEntity::getDimensionId, entry.getValue())
                            .execute())
        );

    }

    private Flux<DimensionUserEntity> publishEvent(Publisher<DimensionUserEntity> stream,
                                                   Function3<String, String, List<String>, AsyncEvent> event) {
        Flux<DimensionUserEntity> cache = Flux.from(stream).doOnNext(DimensionUserEntity::generateId).cache();

        Set<Mono<Void>> jobs = ConcurrentHashMap.newKeySet();

        return cache
                .groupBy(DimensionUserEntity::getDimensionTypeId)
                .flatMap(typeGroup -> {
                    String type = typeGroup.key();
                    return typeGroup
                            .groupBy(DimensionUserEntity::getDimensionId)
                            .flatMap(dimensionIdGroup -> {
                                String dimensionId = dimensionIdGroup.key();
                                return dimensionIdGroup
                                        .map(DimensionUserEntity::getUserId)
                                        .collectList()
                                        .doOnNext(userIdList -> jobs.add(event.apply(type, dimensionId, userIdList).publish(eventPublisher)))
                                        .flatMapIterable(Function.identity());
                            });
                })
                .<Collection<String>>collect(HashSet::new, Collection::add)
                .flatMap(userList -> ClearUserAuthorizationCacheEvent.of(userList).publish(eventPublisher))
                .then(Mono.defer(() -> Flux.concat(jobs).then()))
                .thenMany(cache);
    }

    private Mono<Void> clearUserCache(List<DimensionUserEntity> entities) {
        return Flux.fromIterable(entities)
                   .map(DimensionUserEntity::getUserId)
                   .distinct()
                   .collectList()
                   .flatMap(list -> ClearUserAuthorizationCacheEvent.of(list).publish(eventPublisher));
    }

}