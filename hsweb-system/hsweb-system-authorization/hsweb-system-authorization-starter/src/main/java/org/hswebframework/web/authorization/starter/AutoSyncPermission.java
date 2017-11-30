package org.hswebframework.web.authorization.starter;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.AuthorizeDefinitionInitializedEvent;
import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.entity.authorization.ActionEntity;
import org.hswebframework.web.entity.authorization.PermissionEntity;
import org.hswebframework.web.service.authorization.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class AutoSyncPermission implements ApplicationListener<AuthorizeDefinitionInitializedEvent> {

    @Autowired
    private PermissionService permissionService;

    @Autowired
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

    @Override
    public void onApplicationEvent(AuthorizeDefinitionInitializedEvent event) {
        List<AuthorizeDefinition> definitions = event.getAllDefinition();

        Map<String, List<AuthorizeDefinition>> grouping = new HashMap<>();


        for (AuthorizeDefinition definition : definitions) {
            for (String permissionId : definition.getPermissions()) {
                grouping.computeIfAbsent(permissionId, id -> new ArrayList<>())
                        .add(definition);
            }
        }
        Map<String, PermissionEntity> permissionEntityMap = new HashMap<>();

        for (Map.Entry<String, List<AuthorizeDefinition>> permissionEntiry : grouping.entrySet()) {
            String permissionId = permissionEntiry.getKey();
            List<AuthorizeDefinition> allPermission = permissionEntiry.getValue();
            if (allPermission.isEmpty()) {
                return;
            }
            AuthorizeDefinition tmp = allPermission.get(0);

            List<String> descs = allPermission.stream()
                    .map(AuthorizeDefinition::getActionDescription)
                    .flatMap(Stream::of)
                    .collect(Collectors.toList());

            List<String> actions = allPermission
                    .stream()
                    .map(AuthorizeDefinition::getActions)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            Set<ActionEntity> actionEntities = new HashSet<>(actions.size());
            if (!actions.isEmpty()) {
                for (int i = 0; i < actions.size(); i++) {
                    String action = actions.get(i);
                    String desc = descs.size() > i ? descs.get(i) : actionDescMapping.getOrDefault(actions.get(i), action);
                    ActionEntity actionEntity = new ActionEntity();
                    actionEntity.setAction(action);
                    actionEntity.setDescribe(desc);
                    actionEntities.add(actionEntity);
                }
            }
            PermissionEntity entity = entityFactory.newInstance(PermissionEntity.class);
            entity.setId(permissionId);
            entity.setName(tmp.getPermissionDescription().length > 0 ? tmp.getPermissionDescription()[0] : permissionId);
            entity.setActions(new ArrayList<>(actionEntities));
            entity.setType("default");
            entity.setStatus(DataStatus.STATUS_ENABLED);

            permissionEntityMap.putIfAbsent(entity.getId(), entity);
        }

        Map<String, PermissionEntity> old = permissionService
                .selectByPk(new ArrayList<>(permissionEntityMap.keySet()))
                .stream()
                .collect(Collectors.toMap(PermissionEntity::getId, Function.identity()));

        permissionEntityMap.forEach((permissionId, permission) -> {
            log.info("try sync permission[{}].{}", permissionId, permission.getActions());
            PermissionEntity oldPermission = old.get(permissionId);
            if (oldPermission == null) {
                permissionService.insert(permission);
            } else {
                List<ActionEntity> oldAction = oldPermission.getActions();
                if (oldAction == null) {
                    oldAction = new ArrayList<>();
                }
                Map<String, ActionEntity> actionCache = oldAction
                        .stream().collect(Collectors.toMap(ActionEntity::getAction, Function.identity()));
                boolean permissionChanged = false;
                for (ActionEntity actionEntity : permission.getActions()) {
                    if (actionCache.get(actionEntity.getAction()) == null) {
                        oldAction.add(actionEntity);
                        permissionChanged = true;
                    }
                }
                if (permissionChanged) {
                    oldPermission.setActions(oldAction);

                    permissionService.updateByPk(oldPermission.getId(), oldPermission);
                }
            }

        });

    }
}
