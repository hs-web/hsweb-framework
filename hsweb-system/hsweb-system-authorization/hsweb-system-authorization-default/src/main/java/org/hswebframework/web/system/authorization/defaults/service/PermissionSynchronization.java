package org.hswebframework.web.system.authorization.defaults.service;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.mapping.ReactiveRepository;
import org.hswebframework.ezorm.rdb.mapping.annotation.Comment;
import org.hswebframework.utils.ClassUtils;
import org.hswebframework.web.api.crud.entity.Entity;
import org.hswebframework.web.authorization.define.*;
import org.hswebframework.web.crud.web.reactive.ReactiveCrudController;
import org.hswebframework.web.crud.web.reactive.ReactiveQueryController;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceCrudController;
import org.hswebframework.web.crud.web.reactive.ReactiveServiceQueryController;
import org.hswebframework.web.system.authorization.api.entity.ActionEntity;
import org.hswebframework.web.system.authorization.api.entity.OptionalField;
import org.hswebframework.web.system.authorization.api.entity.PermissionEntity;
import org.hswebframework.web.utils.AnnotationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.persistence.Column;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
public class PermissionSynchronization implements CommandLineRunner {

    @Autowired
    private ReactiveRepository<PermissionEntity, String> permissionRepository;

    private MergedAuthorizeDefinition definition = new MergedAuthorizeDefinition();

    private Map<String, List<OptionalField>> entityFieldsMapping = new HashMap<>();

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
                Class target = ((AopAuthorizeDefinition) authorizeDefinition).getTargetClass();
                if (ReactiveQueryController.class.isAssignableFrom(target)
                        || ReactiveServiceQueryController.class.isAssignableFrom(target)) {
                    Class entity = ClassUtils.getGenericType(target);
                    if (Entity.class.isAssignableFrom(entity)) {
                        Set<OptionalField> fields = new HashSet<>();
                        ReflectionUtils.doWithFields(entity, field -> {
                            if (null != field.getAnnotation(Column.class) && !"id".equals(field.getName())) {
                                OptionalField optionalField = new OptionalField();
                                optionalField.setName(field.getName());
                                Optional.ofNullable(field.getAnnotation(Comment.class))
                                        .map(Comment::value)
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

    protected PermissionEntity convert(Map<String, PermissionEntity> old, ResourceDefinition definition) {
        PermissionEntity entity = old.getOrDefault(definition.getId(), PermissionEntity.builder()
                .name(definition.getName())
                .describe(definition.getDescription())
                .status((byte) 1)
                .build());
        entity.setId(definition.getId());


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
            Map<String, Object> properties = Optional.ofNullable(action.getProperties()).orElse(new HashMap<>());
            Set<Object> types = Optional.of(properties.computeIfAbsent("supportDataAccessTypes", t -> new HashSet<>()))
                    .filter(Collection.class::isInstance)
                    .<Collection<Object>>map(Collection.class::cast)
                    .<Set<Object>>map(HashSet::new)
                    .orElseGet(HashSet::new);

            types.addAll(definitionAction.getDataAccess().getDataAccessTypes().stream().map(DataAccessTypeDefinition::getId).collect(Collectors.toSet()));
            action.setProperties(properties);
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
        permissionRepository.createQuery()
                .fetch()
                .collect(Collectors.toMap(PermissionEntity::getId, Function.identity()))
                .flatMap(group -> Flux.fromIterable(definition.getResources())
                        .map(d -> this.convert(group, d))
                        .as(permissionRepository::save))
                .doOnError(err -> log.warn("sync permission error", err))
                .subscribe(l -> {
                    log.info("sync permission success:{}", l);
                });

    }
}
