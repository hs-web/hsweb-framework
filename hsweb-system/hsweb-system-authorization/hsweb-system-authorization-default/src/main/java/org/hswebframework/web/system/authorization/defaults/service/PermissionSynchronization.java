package org.hswebframework.web.system.authorization.defaults.service;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.utils.ClassUtils;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.authorization.define.*;
import org.hswebframework.web.crud.web.reactive.ReactiveQueryController;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceQueryController;
import org.hswebframework.web.i18n.LocaleUtils;
import org.hswebframework.web.system.authorization.api.entity.ActionEntity;
import org.hswebframework.web.system.authorization.api.entity.OptionalField;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;

import javax.persistence.Column;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class PermissionSynchronization implements CommandLineRunner {

    private final ReactiveRepository<PermissionEntity, String> permissionRepository;

    private final AuthorizeDefinitionCustomizer customizer;

    private final MergedAuthorizeDefinition definition = new MergedAuthorizeDefinition();

    private final Map<String, List<OptionalField>> entityFieldsMapping = new HashMap<>();

    public PermissionSynchronization(ReactiveRepository<PermissionEntity, String> permissionRepository,
                                     AuthorizeDefinitionCustomizer customizer) {
        this.permissionRepository = permissionRepository;
        this.customizer = customizer;
    }

    @EventListener
    public void handleResourceParseEvent(AuthorizeDefinitionInitializedEvent event) {
        definition.merge(event.getAllDefinition());
    }

    public static PermissionEntity convert(Map<String, PermissionEntity> old,
                                           ResourceDefinition definition,
                                           Map<String, List<OptionalField>> entityFieldsMapping) {
        PermissionEntity entity = old.computeIfAbsent(
            definition.getId(),
            _id -> PermissionEntity
                .builder()
                .name(definition.getName())
                .describe(definition.getDescription())
                .i18nMessages(definition.getI18nMessages())
                .status((byte) 1)
                .build());
        entity.setId(definition.getId());

        Map<String, ActionEntity> oldAction = new LinkedHashMap<>();

        if (entity.getActions() != null) {
            for (ActionEntity action : entity.getActions()) {
                oldAction.put(action.getAction(), action);
            }
        }

        for (ResourceActionDefinition definitionAction : definition.getActions()) {
            ActionEntity action = oldAction.getOrDefault(definitionAction.getId(), ActionEntity
                .builder()
                .action(definitionAction.getId())
                .name(definitionAction.getName())
                .describe(definitionAction.getName())
                .build());
            action.setI18nMessages(definitionAction.getI18nMessages());

            oldAction.put(action.getAction(), action);
        }

        entity.setActions(new ArrayList<>(oldAction.values()));

        return entity;
    }


    @Override
    public void run(String... args) throws Exception {
        if (definition.getResources().isEmpty()) {
            return;
        }
        customizer.custom(definition);

        permissionRepository
            .createQuery()
            .fetch()
            .collectMap(PermissionEntity::getId, Function.identity(), ConcurrentHashMap::new)
            .flatMap(group -> Flux
                .fromIterable(definition.getResources())
                .map(d -> PermissionSynchronization.convert(group, d, entityFieldsMapping))
                .as(permissionRepository::save))
            .subscribe(
                l -> log.info("sync permission success:{}", l),
                err -> log.warn("sync permission error", err));

    }
}
