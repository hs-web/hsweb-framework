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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class PermissionSynchronization implements CommandLineRunner {

    private final ReactiveRepository<PermissionEntity, String> permissionRepository;

    private final AuthorizeDefinitionCustomizer customizer;

    private final MergedAuthorizeDefinition definition = new MergedAuthorizeDefinition();

    private final Map<String, List<OptionalField>> entityFieldsMapping = new HashMap<>();

    private final static String resolvePermissionPrefix = "hswebframework.web.system.permission.";
    private final static String resolveActionPrefix = "hswebframework.web.system.action.";

    private final static List<Locale> supportLocale = new ArrayList<>();

    static {
        supportLocale.add(Locale.CHINESE);
        supportLocale.add(Locale.ENGLISH);
    }

    public PermissionSynchronization(ReactiveRepository<PermissionEntity, String> permissionRepository,
                                     AuthorizeDefinitionCustomizer customizer) {
        this.permissionRepository = permissionRepository;
        this.customizer = customizer;
    }

    @EventListener
    public void handleResourceParseEvent(AuthorizeDefinitionInitializedEvent event) {
        definition.merge(event.getAllDefinition());
        for (AuthorizeDefinition authorizeDefinition : event.getAllDefinition()) {
            if (authorizeDefinition.getResources().getResources().isEmpty()) {
                continue;
            }
            String id = authorizeDefinition.getResources().getResources().iterator().next().getId();
            if (entityFieldsMapping.containsKey(id)) {
                return;
            }
            if (authorizeDefinition instanceof AopAuthorizeDefinition) {
                Class<?> target = ((AopAuthorizeDefinition) authorizeDefinition).getTargetClass();
                if (ReactiveQueryController.class.isAssignableFrom(target)
                        || ReactiveServiceQueryController.class.isAssignableFrom(target)) {
                    Class<?> entity = ClassUtils.getGenericType(target);
                    if (Entity.class.isAssignableFrom(entity)) {
                        Set<OptionalField> fields = new HashSet<>();
                        ReflectionUtils.doWithFields(entity, field -> {
                            if (null != field.getAnnotation(Column.class) && !"id".equals(field.getName())) {
                                OptionalField optionalField = new OptionalField();
                                optionalField.setName(field.getName());
                                Optional.ofNullable(field.getAnnotation(Schema.class))
                                        .map(Schema::description)
                                        .ifPresent(optionalField::setDescribe);
                                fields.add(optionalField);
                            }
                        });
                        entityFieldsMapping.put(id, new ArrayList<>(fields));
                    }
                }
            }
        }
    }

    public static PermissionEntity convert(Map<String, PermissionEntity> old, ResourceDefinition definition, Map<String, List<OptionalField>> entityFieldsMapping) {
        PermissionEntity entity = old.getOrDefault(definition.getId(), PermissionEntity.builder()
                                                                                       .name(definition.getName())
                                                                                       .describe(definition.getDescription())
                                                                                       .status((byte) 1)
                                                                                       .build());
        entity.setId(definition.getId());
        entity.setI18nMessages(buildI18nPermissionMessage(definition));

        if (CollectionUtils.isEmpty(entity.getOptionalFields())) {
            entity.setOptionalFields(entityFieldsMapping.get(entity.getId()));
        }

        Map<String, ActionEntity> oldAction = new HashMap<>();
        if (entity.getActions() != null) {
            entity.getActions().forEach(a -> oldAction.put(a.getAction(), a));
        }

        for (ResourceActionDefinition definitionAction : definition.getActions()) {
            ActionEntity action = oldAction.getOrDefault(definitionAction.getId(), ActionEntity
                    .builder()
                    .action(definitionAction.getId())
                    .name(definitionAction.getName())
                    .describe(definitionAction.getName())
                    .build());
            action.setI18nMessages(buildI18nActionMessage(definitionAction));
            Map<String, Object> properties = Optional.ofNullable(action.getProperties()).orElse(new HashMap<>());
            @SuppressWarnings("all")
            Set<Object> types = (Set) Optional
                    .of(properties.computeIfAbsent("supportDataAccessTypes", t -> new HashSet<>()))
                    .filter(Collection.class::isInstance)
                    .map(Collection.class::cast)
                    .map(HashSet::new)
                    .orElseGet(HashSet::new);

            types.addAll(definitionAction
                                 .getDataAccess()
                                 .getDataAccessTypes()
                                 .stream()
                                 .map(DataAccessTypeDefinition::getId)
                                 .collect(Collectors.toSet()));
            action.setProperties(properties);
            oldAction.put(action.getAction(), action);
        }
        entity.setActions(new ArrayList<>(oldAction.values()));

        return entity;
    }

    private static Map<String, String> buildI18nActionMessage(ResourceActionDefinition definition) {
        Map<String, String> i18nMessages = new HashMap<>();
        supportLocale.forEach(locale -> i18nMessages.put(locale.getLanguage(), LocaleUtils.resolveMessage(resolveActionPrefix + definition.getId(), locale, definition.getName())));
        return i18nMessages;
    }

    private static Map<String, String> buildI18nPermissionMessage(ResourceDefinition definition) {
        Map<String, String> i18nMessages = new HashMap<>();
        supportLocale.forEach(locale -> i18nMessages.put(locale.getLanguage(), LocaleUtils.resolveMessage(resolvePermissionPrefix + definition.getId(), locale, definition.getName())));
        return i18nMessages;
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
                .collect(Collectors.toMap(PermissionEntity::getId, Function.identity()))
                .flatMap(group -> Flux.fromIterable(definition.getResources())
                                      .map(d -> PermissionSynchronization.convert(group, d, entityFieldsMapping))
                                      .as(permissionRepository::save))
                .doOnError(err -> log.warn("sync permission error", err))
                .subscribe(l -> {
                    log.info("sync permission success:{}", l);
                });

    }
}
