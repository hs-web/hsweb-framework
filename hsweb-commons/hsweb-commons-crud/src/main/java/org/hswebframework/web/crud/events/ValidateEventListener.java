package org.hswebframework.web.crud.events;

import org.hswebframework.ezorm.rdb.events.EventContext;
import org.hswebframework.ezorm.rdb.events.EventListener;
import org.hswebframework.ezorm.rdb.events.EventType;
import org.hswebframework.ezorm.rdb.mapping.events.MappingContextKeys;
import org.hswebframework.ezorm.rdb.mapping.events.MappingEventTypes;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.validator.CreateGroup;
import org.hswebframework.web.validator.UpdateGroup;

import java.util.List;

public class ValidateEventListener implements EventListener {


    @Override
    @SuppressWarnings("all")
    public void onEvent(EventType type, EventContext context) {
        if (type == MappingEventTypes.insert_before) {

            boolean single= context.get(MappingContextKeys.type).map("single"::equals).orElse(false);
            if(single){
                context.get(MappingContextKeys.instance)
                        .filter(Entity.class::isInstance)
                        .map(Entity.class::cast)
                        .ifPresent(entity -> entity.tryValidate(CreateGroup.class));
            }else{
                context.get(MappingContextKeys.instance)
                        .filter(List.class::isInstance)
                        .map(List.class::cast)
                        .ifPresent(lst ->lst.stream()
                                .filter(Entity.class::isInstance)
                                .map(Entity.class::cast)
                                .forEach(e->((Entity) e).tryValidate(CreateGroup.class))
                        );
            }

        } else if (type == MappingEventTypes.update_before) {
            context.get(MappingContextKeys.instance)
                    .filter(Entity.class::isInstance)
                    .map(Entity.class::cast)
                    .ifPresent(entity -> entity.tryValidate(UpdateGroup.class));
        }

    }
}
