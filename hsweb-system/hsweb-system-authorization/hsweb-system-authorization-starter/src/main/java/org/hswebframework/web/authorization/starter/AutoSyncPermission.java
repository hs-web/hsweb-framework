package org.hswebframework.web.authorization.starter;

import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.utils.ClassUtils;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.define.AopAuthorizeDefinition;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.AuthorizeDefinitionInitializedEvent;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.entity.authorization.ActionEntity;
import org.hswebframework.web.entity.authorization.OptionalField;
import org.hswebframework.web.entity.authorization.PermissionEntity;
import org.hswebframework.web.service.authorization.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AutoSyncPermission implements ApplicationListener<AuthorizeDefinitionInitializedEvent> {


    private PermissionService permissionService;


    private EntityFactory entityFactory;

    private static Map<String, String> actionDescMapping = new HashMap<>();

    static {
        actionDescMapping.put(Permission.ACTION_QUERY, "查询");
        actionDescMapping.put(Permission.ACTION_ADD, "新增");
        actionDescMapping.put(Permission.ACTION_GET, "查看详情");
        actionDescMapping.put(Permission.ACTION_UPDATE, "修改");
        actionDescMapping.put(Permission.ACTION_DELETE, "删除");
        actionDescMapping.put(Permission.ACTION_DISABLE, "禁用");
        actionDescMapping.put(Permission.ACTION_ENABLE, "启用");
        actionDescMapping.put(Permission.ACTION_EXPORT, "导出");
        actionDescMapping.put(Permission.ACTION_IMPORT, "导入");

    }

    @Autowired
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @Autowired
    public void setEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @Override
    public void onApplicationEvent(AuthorizeDefinitionInitializedEvent event) {
        List<AuthorizeDefinition> definitions = event.getAllDefinition();

        Map<String, List<AuthorizeDefinition>> grouping = new HashMap<>();

        //以permissionId分组
        for (AuthorizeDefinition definition : definitions) {
            for (String permissionId : definition.getPermissions()) {
                grouping.computeIfAbsent(permissionId, id -> new ArrayList<>())
                        .add(definition);
            }
        }

        //创建权限实体
        Map<String, PermissionEntity> waitToSyncPermissions = new HashMap<>();
        for (Map.Entry<String, List<AuthorizeDefinition>> permissionDefinition : grouping.entrySet()) {
            String permissionId = permissionDefinition.getKey();
            //一个权限的全部定义(一个permission多个action)
            List<AuthorizeDefinition> allDefinition = permissionDefinition.getValue();
            if (allDefinition.isEmpty()) {
                return;
            }
            AuthorizeDefinition tmp = allDefinition.get(0);
            //action描述
            List<String> actionDescription = allDefinition.stream()
                    .map(AuthorizeDefinition::getActionDescription)
                    .flatMap(Stream::of)
                    .collect(Collectors.toList());
            //action
            List<String> actions = allDefinition
                    .stream()
                    .map(AuthorizeDefinition::getActions)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            //创建action实体
            Set<ActionEntity> actionEntities = new HashSet<>(actions.size());
            if (!actions.isEmpty()) {
                for (int i = 0; i < actions.size(); i++) {
                    String action = actions.get(i);
                    String desc = actionDescription.size() > i ? actionDescription.get(i) : actionDescMapping.getOrDefault(actions.get(i), action);
                    ActionEntity actionEntity = new ActionEntity();
                    actionEntity.setAction(action);
                    actionEntity.setDescribe(desc);
                    actionEntities.add(actionEntity);
                }
            }
            //创建permission
            PermissionEntity entity = entityFactory.newInstance(PermissionEntity.class);
            if (tmp instanceof AopAuthorizeDefinition) {
                AopAuthorizeDefinition aopAuthorizeDefinition = ((AopAuthorizeDefinition) tmp);
                Class type = aopAuthorizeDefinition.getTargetClass();
                Class genType = entityFactory.getInstanceType(ClassUtils.getGenericType(type));
                List<OptionalField> optionalFields = new ArrayList<>();
                entity.setOptionalFields(optionalFields);
                if (genType != Object.class) {
                    List<Field> fields = new ArrayList<>();

                    ReflectionUtils.doWithFields(genType, fields::add, field -> (field.getModifiers() & Modifier.STATIC) == 0);

                    for (Field field : fields) {
                        if ("id".equals(field.getName())) {
                            continue;
                        }
                        ApiModelProperty property = field.getAnnotation(ApiModelProperty.class);
                        OptionalField optionalField = new OptionalField();
                        optionalField.setName(field.getName());
                        if (null != property) {
                            if (property.hidden()) {
                                continue;
                            }
                            optionalField.setDescribe(property.value());
                        }
                        optionalFields.add(optionalField);
                    }
                }
            }
            entity.setId(permissionId);
            entity.setName(tmp.getPermissionDescription().length > 0 ? tmp.getPermissionDescription()[0] : permissionId);
            entity.setActions(new ArrayList<>(actionEntities));
            entity.setType("default");
            entity.setStatus(DataStatus.STATUS_ENABLED);
            waitToSyncPermissions.putIfAbsent(entity.getId(), entity);
        }

        //查询出全部旧的权限数据并载入缓存
        Map<String, PermissionEntity> oldCache = permissionService
                .select()
                .stream()
                .collect(Collectors.toMap(PermissionEntity::getId, Function.identity()));

        waitToSyncPermissions.forEach((permissionId, permission) -> {
            log.info("try sync permission[{}].{}", permissionId, permission.getActions());
            PermissionEntity oldPermission = oldCache.get(permissionId);
            if (oldPermission == null) {
                permissionService.insert(permission);
            } else {
                Set<ActionEntity> oldAction = new HashSet<>();
                if (oldPermission.getActions() != null) {
                    oldAction.addAll(oldPermission.getActions());
                }
                Map<String, ActionEntity> actionCache = oldAction
                        .stream()
                        .collect(Collectors.toMap(ActionEntity::getAction, Function.identity()));
                boolean permissionChanged = false;
                for (ActionEntity actionEntity : permission.getActions()) {
                    //添加新的action到旧的action
                    if (actionCache.get(actionEntity.getAction()) == null) {
                        oldAction.add(actionEntity);
                        permissionChanged = true;
                    }
                }
                if (permissionChanged) {
                    oldPermission.setActions(new ArrayList<>(oldAction));
                    permissionService.updateByPk(oldPermission.getId(), oldPermission);
                }
                actionCache.clear();
            }

        });

        oldCache.clear();
        waitToSyncPermissions.clear();
        definitions.clear();
        grouping.clear();
    }

}
