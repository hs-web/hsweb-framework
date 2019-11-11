package org.hswebframework.web.crud.events;


import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.core.GlobalConfig;
import org.hswebframework.ezorm.rdb.events.*;
import org.hswebframework.ezorm.rdb.mapping.*;
import org.hswebframework.ezorm.rdb.mapping.events.MappingContextKeys;
import org.hswebframework.ezorm.rdb.mapping.events.MappingEventTypes;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.TableOrViewMetadata;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.crud.annotation.EnableEntityEvent;
import org.hswebframework.web.event.GenericsPayloadApplicationEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("all")
public class EntityEventListener implements EventListener {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Override
    public String getId() {
        return "entity-listener";
    }

    @Override
    public String getName() {
        return "实体变更事件监听器";
    }

    @Override
    public void onEvent(EventType type, EventContext context) {

        if (context.get(MappingContextKeys.error).isPresent()) {
            return;
        }
        EntityColumnMapping mapping = context.get(MappingContextKeys.columnMapping).orElse(null);
        if (mapping == null ||
                !Entity.class.isAssignableFrom(mapping.getEntityType()) ||
                mapping.getEntityType().getAnnotation(EnableEntityEvent.class) == null) {
            return;
        }

        if (type == MappingEventTypes.insert_after) {
            boolean single = context.get(MappingContextKeys.type).map("single"::equals).orElse(false);
            if (single) {
                handleInsertSingle(mapping.getEntityType(), context);
            } else {
                handleInsertBatch(mapping.getEntityType(), context);
            }
        }
        if (type == MappingEventTypes.update_before) {
            handleUpdateBefore(context);
        }

        if (type == MappingEventTypes.delete_before) {
            handleDeleteBefore(context);
        }
    }

    protected void sendUpdateEvent(List<?> olds, EventContext context) {
        List<Object> newValues = new ArrayList<>(olds.size());
        EntityColumnMapping mapping = context.get(MappingContextKeys.columnMapping).orElseThrow(UnsupportedOperationException::new);
        TableOrViewMetadata table = context.get(ContextKeys.table).orElseThrow(UnsupportedOperationException::new);
        RDBColumnMetadata idColumn = table.getColumns().stream().filter(RDBColumnMetadata::isPrimaryKey).findFirst().orElse(null);
        if (idColumn == null) {
            return;
        }
        for (Object old : olds) {
            Object newValue = context.get(MappingContextKeys.instance)
                    .filter(Entity.class::isInstance)
                    .map(Entity.class::cast)
                    .orElseGet(() -> {
                        return context.get(MappingContextKeys.updateColumnInstance)
                                .map(map -> {
                                    return FastBeanCopier.copy(map, FastBeanCopier.copy(old, mapping.getEntityType()));
                                })
                                .map(Entity.class::cast)
                                .orElse(null);
                    });
            if (newValue != null) {
                FastBeanCopier.copy(old, newValue, FastBeanCopier.include(idColumn.getAlias()));
            }
            newValues.add(newValue);
        }
        eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, new EntityModifyEvent(olds, newValues, mapping.getEntityType()), mapping.getEntityType()));
    }

    protected void sendDeleteEvent(List<?> olds, EventContext context) {

        EntityColumnMapping mapping = context.get(MappingContextKeys.columnMapping).orElseThrow(UnsupportedOperationException::new);
        TableOrViewMetadata table = context.get(ContextKeys.table).orElseThrow(UnsupportedOperationException::new);
        eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, new EntityDeletedEvent(olds, mapping.getEntityType()), mapping.getEntityType()));

    }

    protected void handleReactiveUpdateBefore(DSLUpdate<?, ?> update, EventContext context) {
        Object repo = context.get(MappingContextKeys.repository).orElse(null);
        if (repo instanceof ReactiveRepository) {
            context.get(MappingContextKeys.reactiveResultHolder)
                    .ifPresent(holder -> {
                        AtomicReference<List<?>> updated = new AtomicReference<>();
                        holder.after(v -> {
                            return Mono.fromRunnable(() -> {
                                List<?> _tmp = updated.getAndSet(null);
                                if (CollectionUtils.isNotEmpty(_tmp)) {
                                    sendUpdateEvent(_tmp, context);
                                }
                            });
                        });
                        holder.before(
                                ((ReactiveRepository<?, ?>) repo).createQuery()
                                        .setParam(update.toQueryParam())
                                        .fetch()
                                        .collectList()
                                        .doOnSuccess(updated::set)
                                        .then()
                        );
                    });
        }
    }

    protected void handleUpdateBefore(EventContext context) {
        context.<DSLUpdate<?, ?>>get(ContextKeys.source())
                .ifPresent(dslUpdate -> {
                    if (context.get(MappingContextKeys.reactive).orElse(false)) {
                        handleReactiveUpdateBefore(dslUpdate, context);
                    } else {
                        // TODO: 2019-11-09
                    }
                });

    }

    protected void handleDeleteBefore(EventContext context) {
        context.<DSLDelete>get(ContextKeys.source())
                .ifPresent(dslUpdate -> {
                    Object repo = context.get(MappingContextKeys.repository).orElse(null);
                    if (repo instanceof ReactiveRepository) {
                        context.get(MappingContextKeys.reactiveResultHolder)
                                .ifPresent(holder -> {
                                    AtomicReference<List<?>> deleted = new AtomicReference<>();
                                    holder.after(v -> {
                                        return Mono.fromRunnable(() -> {
                                            List<?> _tmp = deleted.getAndSet(null);
                                            if (CollectionUtils.isNotEmpty(_tmp)) {
                                                sendDeleteEvent(_tmp, context);
                                            }
                                        });
                                    });
                                    holder.before(
                                            ((ReactiveRepository<?, ?>) repo).createQuery()
                                                    .setParam(dslUpdate.toQueryParam())
                                                    .fetch()
                                                    .collectList()
                                                    .doOnSuccess(deleted::set)
                                                    .then()
                                    );
                                });
                    }
                });
    }

    protected void handleUpdateAfter(EventContext context) {

    }

    protected void handleInsertBatch(Class clazz, EventContext context) {

        context.get(MappingContextKeys.instance)
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .ifPresent(lst -> {
                    eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, new EntityCreatedEvent(lst, clazz), clazz));
                });
    }

    protected void handleInsertSingle(Class clazz, EventContext context) {
        context.get(MappingContextKeys.instance)
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .ifPresent(entity -> {
                    eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, new EntityCreatedEvent(Collections.singletonList(entity), clazz), clazz));
                });
    }
}
