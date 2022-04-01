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
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * 自动填充创建人和修改人信息
 */
public class CreatorEventListener implements EventListener {

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
                resultHolder
                        .ifPresent(holder -> holder
                                .before(
                                        Authentication
                                                .currentReactive()
                                                .doOnNext(auth -> doApplyCreator(type, context, auth))
                                                .then()
                                ));
            } else {
                Authentication
                        .current()
                        .ifPresent(auth -> doApplyCreator(type, context, auth));
            }
        }
    }

    protected void doApplyCreator(EventType type, EventContext context, Authentication auth) {
        context.get(MappingContextKeys.instance)
               .ifPresent(obj -> {
                   if (obj instanceof Collection) {
                       applyCreator(auth, ((Collection<?>) obj), type != MappingEventTypes.update_before);
                   } else {
                       applyCreator(auth, obj, type != MappingEventTypes.update_before);
                   }
               });
    }

    public void applyCreator(Authentication auth, Object entity, boolean updateCreator) {
        if (updateCreator && entity instanceof RecordCreationEntity) {
            RecordCreationEntity e = (RecordCreationEntity) entity;
            if (ObjectUtils.isEmpty(e.getCreatorId())) {
                e.setCreatorId(auth.getUser().getId());
                e.setCreatorName(auth.getUser().getName());
                e.setCreateTimeNow();
            }

        }
        if (entity instanceof RecordModifierEntity) {
            RecordModifierEntity e = (RecordModifierEntity) entity;
            if (ObjectUtils.isEmpty(e.getModifierId())) {
                e.setModifierId(auth.getUser().getId());
                e.setModifierName(auth.getUser().getName());
                e.setModifyTimeNow();
            }
        }
    }

    public void applyCreator(Authentication auth, Collection<?> entities, boolean updateCreator) {
        for (Object entity : entities) {
            applyCreator(auth, entity, updateCreator);
        }

    }
}
