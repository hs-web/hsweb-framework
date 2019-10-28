package org.hswebframework.web.system.authorization.defaults.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.web.authorization.define.*;
import org.hswebframework.web.system.authorization.api.entity.ActionEntity;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class PermissionSynchronization implements CommandLineRunner {

    @Autowired
    private ReactiveRepository<PermissionEntity, String> permissionRepository;

    private MergedAuthorizeDefinition definition = new MergedAuthorizeDefinition();

    @EventListener
    public void handleResourceParseEvent(AuthorizeDefinitionInitializedEvent event) {
        definition.merge(event.getAllDefinition());
    }

    protected PermissionEntity convert(Map<String, PermissionEntity> old, ResourceDefinition definition) {
        PermissionEntity entity = old.getOrDefault(definition.getId(), PermissionEntity.builder()
                .name(definition.getName())
                .describe(definition.getDescription())
                .status((byte) 1)
                .build());


        Map<String, ActionEntity> oldAction = new HashMap<>();
        if (entity.getActions() != null) {
            entity.getActions().forEach(a -> oldAction.put(a.getAction(), a));
        }

        for (ResourceActionDefinition definitionAction : definition.getActions()) {
            ActionEntity action = oldAction.getOrDefault(definition.getId(),ActionEntity
                    .builder()
                    .action(definitionAction.getId())
                    .name(definitionAction.getName())
                    .describe(definitionAction.getName())
                    .build());
            Map<String, Object> properties = Optional.ofNullable(action.getProperties()).orElse(new HashMap<>());
            Set<Object> types = Optional.of(properties.computeIfAbsent("supportDataAccessTypes", t -> new HashSet<>()))
                    .filter(Collection.class::isInstance)
                    .<Collection<Object>>map(Collection.class::cast)
                    .<Set<Object>>map(HashSet::new)
                    .orElseGet(HashSet::new);

            types.addAll(definitionAction.getDataAccess().getDataAccessTypes().stream().map(DataAccessTypeDefinition::getId).collect(Collectors.toSet()));
            action.setProperties(properties);
            oldAction.put(action.getAction(),action);
        }
        entity.setActions(new ArrayList<>(oldAction.values()));


        return entity;
    }

    @Override
    public void run(String... args) throws Exception {
        if (definition.getResources().isEmpty()) {
            return;
        }
        permissionRepository.createQuery()
                .fetch()
                .collect(Collectors.toMap(PermissionEntity::getId, Function.identity()))
                .flatMap(group -> Flux.fromIterable(definition.getResources())
                        .map(d -> this.convert(group, d))
                        .as(permissionRepository::save))
                .doOnError(err -> log.warn("sync permission error", err))
                .subscribe(l -> {
                    log.warn("sync permission success:{}", l);
                });

    }
}
