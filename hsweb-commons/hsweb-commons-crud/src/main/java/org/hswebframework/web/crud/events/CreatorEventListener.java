package org.hswebframework.web.crud.events;

import org.hswebframework.ezorm.rdb.events.EventContext;
import org.hswebframework.ezorm.rdb.events.EventListener;
import org.hswebframework.ezorm.rdb.events.EventType;
import org.hswebframework.ezorm.rdb.mapping.events.MappingContextKeys;
import org.hswebframework.ezorm.rdb.mapping.events.MappingEventTypes;
import org.hswebframework.ezorm.rdb.mapping.events.ReactiveResultHolder;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.api.crud.entity.RecordCreationEntity;
import org.hswebframework.web.api.crud.entity.RecordModifierEntity;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.validator.CreateGroup;
import org.hswebframework.web.validator.UpdateGroup;
import org.springframework.core.Ordered;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.springframework.data.repository.util.ClassUtils.ifPresent;

/**
 * 自动填充创建人和修改人信息
 */
public class CreatorEventListener implements EventListener, Ordered {

    @Override
    public String getId() {
        return "creator-listener";
    }

    @Override
    public String getName() {
        return "创建者监听器";
    }

    @Override
    public void onEvent(EventType type, EventContext context) {
        Optional<ReactiveResultHolder> resultHolder = context.get(MappingContextKeys.reactiveResultHolder);
        if (type == MappingEventTypes.insert_before
            || type == MappingEventTypes.save_before
            || type == MappingEventTypes.update_before) {
            if (resultHolder.isPresent()) {
                ReactiveResultHolder holder = resultHolder.get();

                holder
                    .before(
                        Mono.deferContextual(ctx -> Authentication
                            .currentReactive()
                            .doOnNext(auth -> doApplyCreator(ctx, type, context, auth))
                            .then())
                    );
            } else {
                Authentication
                    .current()
                    .ifPresent(auth -> doApplyCreator(Context.empty(), type, context, auth));
            }
        }
    }

    protected void doApplyCreator(ContextView ctx, EventType type, EventContext context, Authentication auth) {
        Object instance = context.get(MappingContextKeys.instance).orElse(null);
        boolean applyUpdate = !RecordModifierEntity.isDoNotUpdate(ctx);
        if (instance != null) {
            if (instance instanceof Collection) {
                applyCreator(auth, context, ((Collection<?>) instance),
                              type != MappingEventTypes.update_before,applyUpdate);
            } else {
                applyCreator(auth, context, instance,
                              type != MappingEventTypes.update_before,applyUpdate);
            }
        }

        context
            .get(MappingContextKeys.updateColumnInstance)
            .ifPresent(map -> applyCreator(auth, context, map,  type != MappingEventTypes.update_before,applyUpdate));

    }

    public void applyCreator(Authentication auth,
                             EventContext context,
                             Object entity,
                             boolean updateCreator,
                             boolean updateModifier) {
        long now = System.currentTimeMillis();
        if (updateCreator) {
            if (entity instanceof RecordCreationEntity) {
                RecordCreationEntity e = (RecordCreationEntity) entity;
                if (ObjectUtils.isEmpty(e.getCreatorId())) {
                    e.setCreatorId(auth.getUser().getId());
                    e.setCreatorName(auth.getUser().getName());
                }
                if (e.getCreateTime() == null) {
                    e.setCreateTime(now);
                }
            } else if (entity instanceof Map) {
                @SuppressWarnings("all")
                Map<Object, Object> map = ((Map<Object, Object>) entity);
                map.putIfAbsent("creator_id", auth.getUser().getId());
                map.putIfAbsent("creator_name", auth.getUser().getName());
                map.putIfAbsent("create_time", now);
            }


        }
        if (updateModifier){
            if (entity instanceof RecordModifierEntity) {
                RecordModifierEntity e = (RecordModifierEntity) entity;
                if (ObjectUtils.isEmpty(e.getModifierId())) {
                    e.setModifierId(auth.getUser().getId());
                    e.setModifierName(auth.getUser().getName());
                }
                if (e.getModifyTime() == null) {
                    e.setModifyTime(now);
                }
            } else if (entity instanceof Map) {
                @SuppressWarnings("all")
                Map<Object, Object> map = ((Map<Object, Object>) entity);
                map.putIfAbsent("modifier_id", auth.getUser().getId());
                map.putIfAbsent("modifier_name", auth.getUser().getName());
                map.putIfAbsent("modify_time", now);

            }
        }

    }

    public void applyCreator(Authentication auth, EventContext context, Collection<?> entities, boolean updateCreator,boolean updateModifier) {
        for (Object entity : entities) {
            applyCreator(auth, context, entity, updateCreator,updateModifier);
        }

    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
