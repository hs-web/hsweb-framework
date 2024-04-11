package org.hswebframework.web.crud.events;

import org.apache.commons.collections4.MapUtils;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.crud.annotation.EnableEntityEvent;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultEntityEventListenerConfigure implements EntityEventListenerConfigure {

    private final Map<Class<? extends Entity>, Map<EntityEventType, Set<EntityEventPhase>>> enabledFeatures = new ConcurrentHashMap<>();
    private final Map<Class<? extends Entity>, Map<EntityEventType, Set<EntityEventPhase>>> disabledFeatures = new ConcurrentHashMap<>();

    @Override
    public void enable(Class<? extends Entity> entityType) {
        initByEntity(entityType, getOrCreateTypeMap(entityType, enabledFeatures), true);
    }

    @Override
    public void disable(Class<? extends Entity> entityType) {
        enabledFeatures.remove(entityType);
        initByEntity(entityType, getOrCreateTypeMap(entityType, disabledFeatures), true);
    }

    @Override
    public void enable(Class<? extends Entity> entityType, EntityEventType type, EntityEventPhase... feature) {
        if (feature.length == 0) {
            feature = EntityEventPhase.all;
        }
        getOrCreatePhaseSet(type, getOrCreateTypeMap(entityType, enabledFeatures))
                .addAll(Arrays.asList(feature));

        //删除disabled
        Arrays.asList(feature)
              .forEach(getOrCreatePhaseSet(type, getOrCreateTypeMap(entityType, disabledFeatures))::remove);
    }

    @Override
    public void disable(Class<? extends Entity> entityType, EntityEventType type, EntityEventPhase... feature) {
        if (feature.length == 0) {
            feature = EntityEventPhase.all;
        }
        getOrCreatePhaseSet(type, getOrCreateTypeMap(entityType, disabledFeatures))
                .addAll(Arrays.asList(feature));
        //删除enabled
        Arrays.asList(feature)
              .forEach(getOrCreatePhaseSet(type, getOrCreateTypeMap(entityType, enabledFeatures))::remove);
    }

    protected Map<EntityEventType, Set<EntityEventPhase>> getOrCreateTypeMap(Class<? extends Entity> type,
                                                                             Map<Class<? extends Entity>, Map<EntityEventType, Set<EntityEventPhase>>> map) {
        return map.computeIfAbsent(type, ignore -> new EnumMap<>(EntityEventType.class));
    }

    protected Set<EntityEventPhase> getOrCreatePhaseSet(EntityEventType type,
                                                        Map<EntityEventType, Set<EntityEventPhase>> map) {
        return map.computeIfAbsent(type, ignore -> EnumSet.noneOf(EntityEventPhase.class));
    }

    protected void initByEntity(Class<? extends Entity> type,
                                Map<EntityEventType, Set<EntityEventPhase>> typeSetMap,
                                boolean all) {
        EnableEntityEvent annotation = AnnotatedElementUtils.findMergedAnnotation(type, EnableEntityEvent.class);
        EntityEventType[] types = annotation != null ? annotation.value() : all ? EntityEventType.values() : new EntityEventType[0];

        for (EntityEventType entityEventType : types) {
            Set<EntityEventPhase> phases = getOrCreatePhaseSet(entityEventType, typeSetMap);
            phases.addAll(Arrays.asList(EntityEventPhase.values()));
        }
    }

    @Override
    public boolean isEnabled(Class<? extends Entity> entityType) {
        Map<EntityEventType, Set<EntityEventPhase>> enabled = initByEntityType(entityType);
        return MapUtils.isNotEmpty(enabled);
    }

    @Override
    public boolean isEnabled(Class<? extends Entity> entityType,
                             EntityEventType type,
                             EntityEventPhase phase) {
        Map<EntityEventType, Set<EntityEventPhase>> enabled = initByEntityType(entityType);
        if (MapUtils.isEmpty(enabled)) {
            return false;
        }
        Map<EntityEventType, Set<EntityEventPhase>> disabled = disabledFeatures.get(entityType);
        Set<EntityEventPhase> phases = enabled.get(type);
        if (phases != null && phases.contains(phase)) {
            if (disabled != null) {
                Set<EntityEventPhase> disabledPhases = disabled.get(type);
                return disabledPhases == null || !disabledPhases.contains(phase);
            }
            return true;
        }

        return false;
    }

    private Map<EntityEventType, Set<EntityEventPhase>> initByEntityType(Class<? extends Entity> entityType) {
        return enabledFeatures
            .compute(entityType, (k, v) -> {
                if (v != null) {
                    return v;
                }
                v = new EnumMap<>(EntityEventType.class);
                initByEntity(k, v, false);
                return v;
            });
    }
}
