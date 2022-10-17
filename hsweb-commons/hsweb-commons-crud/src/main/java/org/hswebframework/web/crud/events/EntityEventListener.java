package org.hswebframework.web.crud.events;


import lombok.AllArgsConstructor;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.ezorm.core.GlobalConfig;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.ezorm.rdb.events.*;
import org.hswebframework.ezorm.rdb.events.EventListener;
import org.hswebframework.ezorm.rdb.events.EventType;
import org.hswebframework.ezorm.rdb.executor.NullValue;
import org.hswebframework.ezorm.rdb.mapping.*;
import org.hswebframework.ezorm.rdb.mapping.events.MappingContextKeys;
import org.hswebframework.ezorm.rdb.mapping.events.MappingEventTypes;
import org.hswebframework.ezorm.rdb.mapping.events.ReactiveResultHolder;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.TableOrViewMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.NativeSql;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.event.AsyncEvent;
import org.hswebframework.web.event.GenericsPayloadApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.Ordered;
import reactor.core.publisher.Mono;
import reactor.function.Function3;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static org.hswebframework.web.crud.events.EntityEventHelper.*;

@SuppressWarnings("all")
@AllArgsConstructor
public class EntityEventListener implements EventListener, Ordered {

    private final ApplicationEventPublisher eventPublisher;

    private final EntityEventListenerConfigure listenerConfigure;

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
        Class<Entity> entityType;

        if (mapping == null ||
                !Entity.class.isAssignableFrom(entityType = (Class) mapping.getEntityType()) ||
                !listenerConfigure.isEnabled(entityType)) {
            return;
        }

        if (type == MappingEventTypes.select_before) {
            handleQueryBefore(mapping, context);
        }
        if (type == MappingEventTypes.insert_before) {
            boolean single = context.get(MappingContextKeys.type).map("single"::equals).orElse(false);
            if (single) {
                handleSingleOperation(mapping.getEntityType(),
                                      EntityEventType.create,
                                      context,
                                      EntityPrepareCreateEvent::new,
                                      EntityBeforeCreateEvent::new,
                                      EntityCreatedEvent::new);
            } else {
                handleBatchOperation(mapping.getEntityType(),
                                     EntityEventType.save,
                                     context,
                                     EntityPrepareSaveEvent::new,
                                     EntityBeforeCreateEvent::new,
                                     EntityCreatedEvent::new);
            }
        }
        if (type == MappingEventTypes.save_before) {
            boolean single = context.get(MappingContextKeys.type).map("single"::equals).orElse(false);
            if (single) {
                handleSingleOperation(mapping.getEntityType(),
                                      EntityEventType.save,
                                      context,

                                      EntityPrepareSaveEvent::new,
                                      EntityBeforeSaveEvent::new,
                                      EntitySavedEvent::new);
            } else {
                handleBatchOperation(mapping.getEntityType(),
                                     EntityEventType.save,
                                     context,
                                     EntityPrepareSaveEvent::new,
                                     EntityBeforeSaveEvent::new,
                                     EntitySavedEvent::new);
            }
        }
        if (type == MappingEventTypes.update_before) {
            handleUpdateBefore(context);
        }
        if (type == MappingEventTypes.delete_before) {
            handleDeleteBefore(entityType, context);
        }
    }

    protected void handleQueryBefore(EntityColumnMapping mapping, EventContext context) {
        context.get(MappingContextKeys.reactiveResultHolder)
               .ifPresent(holder -> {
                   context.get(MappingContextKeys.queryOaram)
                          .ifPresent(queryParam -> {
                              EntityBeforeQueryEvent event = new EntityBeforeQueryEvent<>(queryParam, mapping.getEntityType());
                              eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, event, mapping.getEntityType()));
                              holder
                                      .before(
                                              event.getAsync()
                                      );
                          });
               });
    }

    protected List<Object> createAfterData(List<Object> olds,
                                           EventContext context) {
        List<Object> newValues = new ArrayList<>(olds.size());

        EntityColumnMapping mapping = context
                .get(MappingContextKeys.columnMapping)
                .orElseThrow(UnsupportedOperationException::new);

        Map<String, Object> columns = context
                .get(MappingContextKeys.updateColumnInstance)
                .orElse(Collections.emptyMap());

        for (Object old : olds) {
            Object data = FastBeanCopier.copy(old, mapping.newInstance());
            for (Map.Entry<String, Object> entry : columns.entrySet()) {

                RDBColumnMetadata column = mapping.getColumnByName(entry.getKey()).orElse(null);
                if (column == null) {
                    continue;
                }

                Object value = entry.getValue();

                //set null
                if (value instanceof NullValue) {
                    value = null;
                }

                GlobalConfig
                        .getPropertyOperator()
                        .setProperty(data, column.getAlias(), value);

            }
            newValues.add(data);
        }
        return newValues;
    }

    protected Mono<Void> sendUpdateEvent(List<Object> before,
                                         List<Object> after,
                                         Class<Object> type,
                                         Function3<List<Object>, List<Object>, Class<Object>, AsyncEvent> mapper) {

        return publishEvent(this,
                            type,
                            () -> mapper.apply(before, after, type),
                            eventPublisher::publishEvent);
    }

    protected Mono<Void> sendDeleteEvent(List<Object> olds,
                                         Class<Object> type,
                                         BiFunction<List<Object>, Class<Object>, AsyncEvent> eventBuilder) {
        return publishEvent(this,
                            type,
                            () -> eventBuilder.apply(olds, type),
                            eventPublisher::publishEvent);
    }

    protected void handleUpdateBefore(DSLUpdate<?, ?> update, EventContext context) {
        Object repo = context.get(MappingContextKeys.repository).orElse(null);
        EntityColumnMapping mapping = context
                .get(MappingContextKeys.columnMapping)
                .orElseThrow(UnsupportedOperationException::new);
        Class entityType = (Class) mapping.getEntityType();
        if (repo instanceof ReactiveRepository) {

            context.get(MappingContextKeys.reactiveResultHolder)
                   .ifPresent(holder -> {
                       AtomicReference<Tuple2<List<Object>, List<Object>>> updated = new AtomicReference<>();
                       //prepare
                       if (isEnabled(entityType,
                                     EntityEventType.modify,
                                     EntityEventPhase.prepare,
                                     EntityEventPhase.before,
                                     EntityEventPhase.after)) {
                           holder.before(
                                   this.doAsyncEvent(() -> ((ReactiveRepository<Object, ?>) repo)
                                           .createQuery()
                                           .setParam(update.toQueryParam())
                                           .fetch()
                                           .collectList()
                                           .flatMap((list) -> {
                                               List<Object> after = createAfterData(list, context);
                                               updated.set(Tuples.of(list, after));
                                               return sendUpdateEvent(list,
                                                                      after,
                                                                      entityType,
                                                                      EntityPrepareModifyEvent::new);

                                           }).then()
                                   )
                           );
                       }
                       //before
                       if (isEnabled(entityType, EntityEventType.modify, EntityEventPhase.before)) {
                           holder.invoke(this.doAsyncEvent(() -> {
                               Tuple2<List<Object>, List<Object>> _tmp = updated.get();
                               if (_tmp != null) {

                                   return sendUpdateEvent(_tmp.getT1(),
                                                          _tmp.getT2(),
                                                          entityType,
                                                          EntityBeforeModifyEvent::new);
                               }
                               return Mono.empty();
                           }));
                       }

                       //after
                       if (isEnabled(entityType, EntityEventType.modify, EntityEventPhase.after)) {
                           holder.after(v -> this
                                   .doAsyncEvent(() -> {
                                       Tuple2<List<Object>, List<Object>> _tmp = updated.getAndSet(null);
                                       if (_tmp != null) {
                                           return sendUpdateEvent(_tmp.getT1(),
                                                                  _tmp.getT2(),
                                                                  entityType,
                                                                  EntityModifyEvent::new);
                                       }
                                       return Mono.empty();
                                   }));
                       }

                   });
        } else if (repo instanceof SyncRepository) {
            if (isEnabled(entityType, EntityEventType.modify, EntityEventPhase.before)) {
                QueryParam param = update.toQueryParam();
                SyncRepository<Object, ?> syncRepository = ((SyncRepository<Object, ?>) repo);
                List<Object> list = syncRepository.createQuery()
                                                  .setParam(param)
                                                  .fetch();
                sendUpdateEvent(list,
                                createAfterData(list, context),
                                (Class<Object>) mapping.getEntityType(),
                                EntityBeforeModifyEvent::new)
                        .block();
            }
        }
    }

    protected void handleUpdateBefore(EventContext context) {
        context.<DSLUpdate<?, ?>>get(ContextKeys.source())
               .ifPresent(dslUpdate -> {
                   handleUpdateBefore(dslUpdate, context);
               });

    }

    protected void handleDeleteBefore(Class<Entity> entityType, EventContext context) {
        EntityColumnMapping mapping = context
                .get(MappingContextKeys.columnMapping)
                .orElseThrow(UnsupportedOperationException::new);
        context.<DSLDelete>get(ContextKeys.source())
               .ifPresent(dslUpdate -> {
                   Object repo = context.get(MappingContextKeys.repository).orElse(null);
                   if (repo instanceof ReactiveRepository) {
                       context.get(MappingContextKeys.reactiveResultHolder)
                              .ifPresent(holder -> {
                                  AtomicReference<List<Object>> deleted = new AtomicReference<>();
                                  if (isEnabled(entityType, EntityEventType.delete, EntityEventPhase.before, EntityEventPhase.after)) {
                                      holder.before(
                                              this.doAsyncEvent(() -> ((ReactiveRepository<Object, ?>) repo)
                                                      .createQuery()
                                                      .setParam(dslUpdate.toQueryParam())
                                                      .fetch()
                                                      .collectList()
                                                      .filter(CollectionUtils::isNotEmpty)
                                                      .flatMap(list -> {
                                                          deleted.set(list);
                                                          return this
                                                                  .sendDeleteEvent(list, (Class) mapping.getEntityType(), EntityBeforeDeleteEvent::new);
                                                      })
                                              )
                                      );
                                  }
                                  if (isEnabled(entityType, EntityEventType.delete, EntityEventPhase.after)) {
                                      holder.after(v -> this
                                              .doAsyncEvent(() -> {
                                                  List<Object> _tmp = deleted.getAndSet(null);
                                                  if (CollectionUtils.isNotEmpty(_tmp)) {
                                                      return sendDeleteEvent(_tmp, (Class) mapping.getEntityType(), EntityDeletedEvent::new);
                                                  }
                                                  return Mono.empty();
                                              }));
                                  }

                              });
                   } else if (repo instanceof SyncRepository) {
                       QueryParam param = dslUpdate.toQueryParam();
                       SyncRepository<Object, ?> syncRepository = ((SyncRepository<Object, ?>) repo);
                       List<Object> list = syncRepository.createQuery()
                                                         .setParam(param)
                                                         .fetch();
                       this.sendDeleteEvent(list, (Class) mapping.getEntityType(), EntityBeforeDeleteEvent::new)
                           .block();
                   }
               });
    }

    protected void handleUpdateAfter(EventContext context) {

    }

    protected void handleBatchOperation(Class clazz,
                                        EntityEventType entityEventType,
                                        EventContext context,
                                        BiFunction<List<?>, Class, AsyncEvent> before,
                                        BiFunction<List<?>, Class, AsyncEvent> execute,
                                        BiFunction<List<?>, Class, AsyncEvent> after) {

        context.get(MappingContextKeys.instance)
               .filter(List.class::isInstance)
               .map(List.class::cast)
               .ifPresent(lst -> {
                   AsyncEvent prepareEvent = before.apply(lst, clazz);
                   AsyncEvent afterEvent = after.apply(lst, clazz);
                   AsyncEvent beforeEvent = execute.apply(lst, clazz);
                   Object repo = context.get(MappingContextKeys.repository).orElse(null);
                   if (repo instanceof ReactiveRepository) {
                       Optional<ReactiveResultHolder> resultHolder = context.get(MappingContextKeys.reactiveResultHolder);
                       if (resultHolder.isPresent()) {
                           ReactiveResultHolder holder = resultHolder.get();
                           if (null != prepareEvent && isEnabled(clazz, entityEventType, EntityEventPhase.prepare)) {
                               holder.before(
                                       this.doAsyncEvent(() -> {
                                           return publishEvent(this,
                                                               clazz,
                                                               () -> prepareEvent,
                                                               eventPublisher::publishEvent);
                                       })
                               );
                           }

                           if (null != beforeEvent && isEnabled(clazz, entityEventType, EntityEventPhase.before)) {
                               holder.invoke(
                                       this.doAsyncEvent(() -> {
                                           return publishEvent(this,
                                                               clazz,
                                                               () -> beforeEvent,
                                                               eventPublisher::publishEvent);
                                       })
                               );
                           }
                           if (null != afterEvent && isEnabled(clazz, entityEventType, EntityEventPhase.after)) {
                               holder.after(v -> {
                                   return this.doAsyncEvent(() -> {
                                       return publishEvent(this,
                                                           clazz,
                                                           () -> afterEvent,
                                                           eventPublisher::publishEvent);
                                   });
                               });
                           }
                           return;
                       }
                   }
                   eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, afterEvent, clazz));
                   //block非响应式的支持
                   afterEvent.getAsync().block();
               });
    }

    boolean isEnabled(Class clazz, EntityEventType entityEventType, EntityEventPhase... phase) {
        for (EntityEventPhase entityEventPhase : phase) {
            if (listenerConfigure.isEnabled(clazz, entityEventType, entityEventPhase)) {
                return true;
            }
        }
        return false;
    }

    protected void handleSingleOperation(Class clazz,
                                         EntityEventType entityEventType,
                                         EventContext context,
                                         BiFunction<List<?>, Class, AsyncEvent> before,
                                         BiFunction<List<?>, Class, AsyncEvent> execute,
                                         BiFunction<List<?>, Class, AsyncEvent> after) {
        context.get(MappingContextKeys.instance)
               .filter(Entity.class::isInstance)
               .map(Entity.class::cast)
               .ifPresent(entity -> {
                   AsyncEvent prepareEvent = before.apply(Collections.singletonList(entity), clazz);
                   AsyncEvent beforeEvent = execute.apply(Collections.singletonList(entity), clazz);
                   AsyncEvent afterEvent = after.apply(Collections.singletonList(entity), clazz);

                   Object repo = context.get(MappingContextKeys.repository).orElse(null);
                   if (repo instanceof ReactiveRepository) {
                       Optional<ReactiveResultHolder> resultHolder = context.get(MappingContextKeys.reactiveResultHolder);
                       if (resultHolder.isPresent()) {
                           ReactiveResultHolder holder = resultHolder.get();
                           if (null != prepareEvent && isEnabled(clazz, entityEventType, EntityEventPhase.prepare)) {
                               holder.before(
                                       this.doAsyncEvent(() -> {
                                           return publishEvent(this,
                                                               clazz,
                                                               () -> prepareEvent,
                                                               eventPublisher::publishEvent);
                                       })
                               );
                           }

                           if (null != beforeEvent && isEnabled(clazz, entityEventType, EntityEventPhase.before)) {
                               holder.invoke(
                                       this.doAsyncEvent(() -> {
                                           return publishEvent(this,
                                                               clazz,
                                                               () -> beforeEvent,
                                                               eventPublisher::publishEvent);
                                       })
                               );
                           }
                           if (null != afterEvent && isEnabled(clazz, entityEventType, EntityEventPhase.after)) {
                               holder.after(v -> {
                                   return this.doAsyncEvent(() -> {
                                       return publishEvent(this,
                                                           clazz,
                                                           () -> afterEvent,
                                                           eventPublisher::publishEvent);
                                   });
                               });
                           }
                           return;
                       }
                   }
                   eventPublisher.publishEvent(new GenericsPayloadApplicationEvent<>(this, afterEvent, clazz));
                   //block非响应式的支持
                   afterEvent.getAsync().block();
               });
    }

    protected Mono<Void> doAsyncEvent(Supplier<Mono<Void>> eventSupplier) {
        return isDoFireEvent(true)
                .filter(Boolean::booleanValue)
                .flatMap(ignore -> {
                    return eventSupplier.get();
                });
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
