/*
 *  Copyright 2016 http://www.hswebframework.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationInitializeService;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.simple.SimpleAuthentication;
import org.hswebframework.web.authorization.simple.SimplePermission;
import org.hswebframework.web.authorization.simple.SimpleRole;
import org.hswebframework.web.authorization.simple.SimpleUser;
import org.hswebframework.web.commons.entity.TreeSupportEntity;
import org.hswebframework.web.dao.authorization.AuthorizationSettingDao;
import org.hswebframework.web.dao.authorization.AuthorizationSettingDetailDao;
import org.hswebframework.web.entity.authorization.*;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLDeleteService;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.authorization.*;
import org.hswebframework.web.service.authorization.AuthorizationSettingTypeSupplier.SettingInfo;
import org.hswebframework.web.service.authorization.events.ClearUserAuthorizationCacheEvent;
import org.hswebframework.web.validator.group.CreateGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.hswebframework.web.commons.entity.DataStatus.STATUS_ENABLED;
import static org.hswebframework.web.entity.authorization.AuthorizationSettingDetailEntity.*;
import static org.hswebframework.web.entity.authorization.AuthorizationSettingEntity.settingFor;
import static org.hswebframework.web.entity.authorization.AuthorizationSettingEntity.type;
import static org.hswebframework.web.service.authorization.simple.CacheConstants.USER_AUTH_CACHE_NAME;
import static org.hswebframework.web.service.authorization.simple.CacheConstants.USER_MENU_CACHE_NAME;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("authorizationSettingService")
public class SimpleAuthorizationSettingService extends GenericEntityService<AuthorizationSettingEntity, String>
        implements AuthorizationSettingService, AuthenticationInitializeService, UserMenuManagerService {

    private AuthorizationSettingDao authorizationSettingDao;

    private AuthorizationSettingDetailDao authorizationSettingDetailDao;

    private AuthorizationSettingMenuService authorizationSettingMenuService;

    private MenuService menuService;

    private UserService userService;

    private PermissionService permissionService;

    private List<AuthorizationSettingTypeSupplier> authorizationSettingTypeSuppliers;

    private DataAccessFactory dataAccessFactory;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public AuthorizationSettingDao getDao() {
        return authorizationSettingDao;
    }

    @Override
    public AuthorizationSettingEntity select(String type, String settingFor) {
        tryValidateProperty(type != null, AuthorizationSettingEntity.type, "{can not be null}");
        tryValidateProperty(settingFor != null, AuthorizationSettingEntity.settingFor, "{can not be null}");
        return createQuery().where(AuthorizationSettingEntity.type, type)
                .and(AuthorizationSettingEntity.settingFor, settingFor)
                .single();
    }

    @Override
    @CacheEvict(cacheNames = {CacheConstants.USER_AUTH_CACHE_NAME, CacheConstants.USER_MENU_CACHE_NAME}, allEntries = true)
    public String saveOrUpdate(AuthorizationSettingEntity entity) {
        AuthorizationSettingEntity old = select(entity.getType(), entity.getSettingFor());
        if (old != null) {
            updateByPk(old.getId(), entity);
            return old.getId();
        }
        return insert(entity);
    }

    @Override
    @CacheEvict(cacheNames = {CacheConstants.USER_AUTH_CACHE_NAME, CacheConstants.USER_MENU_CACHE_NAME}, allEntries = true)
    public String insert(AuthorizationSettingEntity entity) {
        tryValidateProperty(select(entity.getType(), entity.getSettingFor()) == null, AuthorizationSettingEntity.settingFor, "存在相同的配置!");
        entity.setStatus(STATUS_ENABLED);
        String id = super.insert(entity);
        if (entity.getMenus() != null) {
            TreeSupportEntity.forEach(entity.getMenus(), menu -> {
                menu.setStatus(STATUS_ENABLED);
                menu.setSettingId(id);
            });
            authorizationSettingMenuService.insertBatch(entity.getMenus());
        }
        if (entity.getDetails() != null) {
            for (AuthorizationSettingDetailEntity detail : entity.getDetails()) {
                detail.setId(getIDGenerator().generate());
                detail.setSettingId(id);
                detail.setStatus(STATUS_ENABLED);
                tryValidate(detail, CreateGroup.class);
                authorizationSettingDetailDao.insert(detail);
            }
        }
        return id;
    }

    @Override
    @CacheEvict(cacheNames = {CacheConstants.USER_AUTH_CACHE_NAME, CacheConstants.USER_MENU_CACHE_NAME}, allEntries = true)
    public int updateByPk(String id, AuthorizationSettingEntity entity) {
        int size = super.updateByPk(id, entity);
        if (entity.getMenus() != null) {
            authorizationSettingMenuService.deleteBySettingId(id);
            TreeSupportEntity.forEach(entity.getMenus(), menu -> {
                menu.setStatus(STATUS_ENABLED);
                menu.setSettingId(id);
            });
            authorizationSettingMenuService.insertBatch(entity.getMenus());
        }
        if (entity.getDetails() != null) {
            DefaultDSLDeleteService
                    .createDelete(authorizationSettingDetailDao)
                    .where(settingId, id)
                    .exec();
            for (AuthorizationSettingDetailEntity detail : entity.getDetails()) {
                detail.setId(getIDGenerator().generate());
                detail.setSettingId(id);
                detail.setStatus(STATUS_ENABLED);
                tryValidate(detail, CreateGroup.class);
                authorizationSettingDetailDao.insert(detail);
            }
        }
        return size;
    }

    @Override
    @CacheEvict(cacheNames = {CacheConstants.USER_AUTH_CACHE_NAME, CacheConstants.USER_MENU_CACHE_NAME}, allEntries = true)
    public AuthorizationSettingEntity deleteByPk(String id) {
        Objects.requireNonNull(id, "id can not be null");
        authorizationSettingMenuService.deleteBySettingId(id);
        DefaultDSLDeleteService.createDelete(authorizationSettingDetailDao)
                .where(AuthorizationSettingDetailEntity.settingId, id).exec();
        return super.deleteByPk(id);
    }


    private List<AuthorizationSettingEntity> getUserSetting(String userId) {
        Map<String, List<SettingInfo>> settingInfo =
                authorizationSettingTypeSuppliers.stream()
                        .map(supplier -> supplier.get(userId))
                        .flatMap(Set::stream)
                        .collect(Collectors.groupingBy(SettingInfo::getType));
        Stream<Map.Entry<String, List<SettingInfo>>> settingInfoStream = settingInfo.entrySet().stream();
        //大于1 使用并行处理
        if (settingInfo.size() > 1) {
            settingInfoStream = settingInfoStream.parallel();
        }
        return settingInfoStream
                .map(entry ->
                        createQuery()
                                // where type = ? and setting_for in (?,?,?....)
                                .where(type, entry.getKey())
                                .and()
                                .in(settingFor, entry.getValue().stream().map(SettingInfo::getSettingFor).collect(Collectors.toList()))
                                .listNoPaging())
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(cacheNames = USER_MENU_CACHE_NAME, key = "'user-menu-list:'+#userId")
    public List<UserMenuEntity> getUserMenuAsList(String userId) {
        if (null == userId) {
            return new java.util.ArrayList<>();
        }
        UserEntity userEntity = userService.selectByPk(userId);
        if (userEntity == null) {
            return new java.util.ArrayList<>();
        }
        List<AuthorizationSettingEntity> entities = getUserSetting(userId);
        if (entities.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        //用户持有的权限设置id集合
        List<String> settingIdList = entities.stream()
                .map(AuthorizationSettingEntity::getId)
                .collect(Collectors.toList());
        //获取全部设置的菜单
        List<AuthorizationSettingMenuEntity> menuEntities = authorizationSettingMenuService
                .selectBySettingId(settingIdList);
        //得到菜单id
        List<String> menuIdList = menuEntities.stream()
                .map(AuthorizationSettingMenuEntity::getMenuId)
                .distinct()
                .collect(Collectors.toList());
        if (menuIdList.isEmpty()) {
            return new ArrayList<>();
        }
        //获取全部菜单,并创建缓存备用
        Map<String, MenuEntity> menuCache = menuService
                .selectByPk(menuIdList)
                .stream()
                .collect(Collectors.toMap(MenuEntity::getId, Function.identity()));

        //根据配置,重新构造菜单结构
        List<UserMenuEntity> reBuildMenu = new LinkedList<>();
        for (MenuEntity menuEntity : menuCache.values()) {
            UserMenuEntity menu = entityFactory.newInstance(UserMenuEntity.class, menuEntity);
            menu.setSortIndex(menuEntity.getSortIndex());
            menu.setLevel(menuEntity.getLevel());
            menu.setId(menuEntity.getId());
            menu.setParentId(menuEntity.getParentId());
            menu.setMenuId(menuEntity.getId());
            reBuildMenu.add(menu);
        }

//        for (AuthorizationSettingMenuEntity entity : menuEntities) {
//            MenuEntity cache = menuCache.get(entity.getMenuId());
//            if (null != cache && DataStatus.STATUS_ENABLED.equals(cache.getStatus())) {
//                UserMenuEntity menu = entityFactory.newInstance(UserMenuEntity.class, cache);
//                menu.setSortIndex(entity.getSortIndex());
//                menu.setLevel(entity.getLevel());
//                menu.setId(entity.getId());
//                menu.setParentId(entity.getParentId());
//                menu.setMenuId(cache.getId());
//                reBuildMenu.add(menu);
//            }
//        }
        Collections.sort(reBuildMenu);
        return reBuildMenu;
    }

    @Override
    @Cacheable(cacheNames = USER_MENU_CACHE_NAME, key = "'menu-tree:'+#userId")
    public List<UserMenuEntity> getUserMenuAsTree(String userId) {
        return TreeSupportEntity.list2tree(getUserMenuAsList(userId), UserMenuEntity::setChildren,
                (Predicate<UserMenuEntity>) menuEntity ->
                        // parentId为空或者为-1的菜单则认为是根菜单
                        StringUtils.isEmpty(menuEntity.getParentId()) || "-1".equals(menuEntity.getParentId()));
    }

    @TransactionalEventListener(condition = "#event.all")
    @Caching(evict = {
            @CacheEvict(cacheNames = USER_MENU_CACHE_NAME, allEntries = true),
            @CacheEvict(cacheNames = USER_AUTH_CACHE_NAME, allEntries = true)
    })
    public void clearAllUserCache(ClearUserAuthorizationCacheEvent event) {
        logger.debug("clear all user authorization cache");
    }

    @TransactionalEventListener(condition = "!#event.all")
    @Caching(
            evict = {
                    @CacheEvict(value = CacheConstants.USER_AUTH_CACHE_NAME, key = "#event.getUserId()"),
                    @CacheEvict(value = CacheConstants.USER_MENU_CACHE_NAME, key = "'user-menu-list:'+#event.getUserId()"),
                    @CacheEvict(value = CacheConstants.USER_MENU_CACHE_NAME, key = "'menu-tree:'+#event.getUserId()")
            }
    )
    public void clearUserCache(ClearUserAuthorizationCacheEvent event) {
        logger.debug("clear user:{} authorization cache", event.getUserId());
    }


    @Override
    public Authentication initUserAuthorization(String userId) {
        if (null == userId) {
            return null;
        }
        UserEntity userEntity = userService.selectByPk(userId);
        if (userEntity == null) {
            return null;
        }
        SimpleAuthentication authentication = new SimpleAuthentication();
        // 用户信息
        authentication.setUser(SimpleUser.builder()
                .id(userId)
                .username(userEntity.getUsername())
                .name(userEntity.getName())
                .type("default")
                .build());
        //角色
        authentication.setRoles(userService.getUserRole(userId)
                .stream()
                .map(role -> new SimpleRole(role.getId(), role.getName()))
                .collect(Collectors.toList()));

        List<String> settingIdList = getUserSetting(userId)
                .stream()
                .map(AuthorizationSettingEntity::getId)
                .collect(Collectors.toList());

        if (settingIdList.isEmpty()) {
            authentication.setPermissions(new ArrayList<>());
            return authentication;
        }

        // where status=1 and setting_id in (?,?,?)
        List<AuthorizationSettingDetailEntity> detailList = DefaultDSLQueryService
                .createQuery(authorizationSettingDetailDao)
                .where(status, STATE_OK)
                .and().in(settingId, settingIdList)
                .listNoPaging();

        authentication.setPermissions(initPermission(detailList));

        return authentication;
    }

    @Override
    public List<Permission> initPermission(String type, String settingFor) {
        AuthorizationSettingEntity entity = select(type, settingFor);
        if (entity == null) {
            return new ArrayList<>();
        }
        List<AuthorizationSettingDetailEntity> detailList = DefaultDSLQueryService
                .createQuery(authorizationSettingDetailDao)
                .where(status, STATE_OK)
                .and().is(settingId, entity.getId())
                .listNoPaging();
        if (CollectionUtils.isEmpty(detailList)) {
            return new ArrayList<>();
        }
        return initPermission(detailList);
    }

    private List<Permission> initPermission(List<AuthorizationSettingDetailEntity> detailList) {
        //权限id集合
        List<String> permissionIds = detailList.stream()
                .map(AuthorizationSettingDetailEntity::getPermissionId)
                .distinct()
                .collect(Collectors.toList());
        //权限信息缓存
        Map<String, PermissionEntity> permissionEntityCache =
                permissionService.selectByPk(permissionIds)
                        .stream()
                        .collect(Collectors.toMap(PermissionEntity::getId, Function.identity()));

        //防止越权
        detailList = detailList.stream().filter(detail -> {
            PermissionEntity entity = permissionEntityCache.get(detail.getPermissionId());
            if (entity == null || !STATUS_ENABLED.equals(entity.getStatus())) {
                return false;
            }
            List<String> allActions = entity.getActions().stream().map(ActionEntity::getAction).collect(Collectors.toList());

            if (isNotEmpty(entity.getActions()) && isNotEmpty(detail.getActions())) {

                detail.setActions(detail.getActions().stream().filter(allActions::contains).collect(Collectors.toSet()));
            }
            if (isEmpty(entity.getSupportDataAccessTypes())) {
                detail.setDataAccesses(new java.util.ArrayList<>());
            } else if (isNotEmpty(detail.getDataAccesses()) && !entity.getSupportDataAccessTypes().contains("*")) {
                //重构为权限支持的数据权限控制方式,防止越权设置权限
                detail.setDataAccesses(detail
                        .getDataAccesses()
                        .stream()
                        .filter(access ->
                                //以设置支持的权限开头就认为拥有该权限
                                //比如支持的权限为CUSTOM_SCOPE_ORG_SCOPE
                                //设置的权限为CUSTOM_SCOPE 则通过检验
                                entity.getSupportDataAccessTypes().stream()
                                        .anyMatch(type -> type.startsWith(access.getType())))
                        .collect(Collectors.toList()));
            }
            return true;
        }).collect(Collectors.toList());

        //全部权限设置
        Map<String, List<AuthorizationSettingDetailEntity>> settings = detailList
                .stream()
                .collect(Collectors.groupingBy(AuthorizationSettingDetailEntity::getPermissionId));

        List<Permission> permissions = new ArrayList<>();
        //获取关联的权限信息
        Map<String, List<ParentPermission>> parentsPermissions = permissionEntityCache.values().stream()
                .map(PermissionEntity::getParents)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(ParentPermission::getPermission));

        settings.forEach((permissionId, details) -> {
            SimplePermission permission = new SimplePermission();
            permission.setId(permissionId);
            Set<String> actions = new HashSet<>();
            Set<DataAccessConfig> dataAccessConfigs = new HashSet<>();
            //排序,根据优先级进行排序
            Collections.sort(details);
            for (AuthorizationSettingDetailEntity detail : details) {
                //如果指定不合并相同的配置,则清空之前的配置
                if (Boolean.FALSE.equals(detail.getMerge())) {
                    actions.clear();
                    dataAccessConfigs.clear();
                }
                // actions
                if (null != detail.getActions()) {
                    actions.addAll(detail.getActions());
                }
                // 数据权限控制配置
                if (null != detail.getDataAccesses()) {
                    dataAccessConfigs.addAll(detail.getDataAccesses()
                            .stream()
                            .map(dataAccessFactory::create)
                            .collect(Collectors.toSet()));
                }
            }
            //是否有其他权限关联了此权限
            List<ParentPermission> parents = parentsPermissions.get(permissionId);
            if (parents != null) {
                actions.addAll(parents.stream()
                        .map(ParentPermission::getActions)
                        .filter(Objects::nonNull)
                        .flatMap(Collection::stream)
                        .collect(Collectors.toSet()));
                parentsPermissions.remove(permissionId);
            }
            permission.setActions(actions);
            permission.setDataAccesses(dataAccessConfigs);
            permissions.add(permission);
        });

        //关联权限
        parentsPermissions.forEach((per, all) -> {
            SimplePermission permission = new SimplePermission();
            permission.setId(per);
            permission.setActions(all.stream()
                    .map(ParentPermission::getActions)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet()));
            permissions.add(permission);
        });
        return permissions;
    }


    @Autowired
    public void setDataAccessFactory(DataAccessFactory dataAccessFactory) {
        this.dataAccessFactory = dataAccessFactory;
    }

    @Autowired
    public void setAuthorizationSettingTypeSuppliers(List<AuthorizationSettingTypeSupplier> authorizationSettingTypeSuppliers) {
        this.authorizationSettingTypeSuppliers = authorizationSettingTypeSuppliers;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setAuthorizationSettingDao(AuthorizationSettingDao authorizationSettingDao) {
        this.authorizationSettingDao = authorizationSettingDao;
    }

    @Autowired
    public void setAuthorizationSettingDetailDao(AuthorizationSettingDetailDao authorizationSettingDetailDao) {
        this.authorizationSettingDetailDao = authorizationSettingDetailDao;
    }

    @Autowired
    public void setAuthorizationSettingMenuService(AuthorizationSettingMenuService authorizationSettingMenuService) {
        this.authorizationSettingMenuService = authorizationSettingMenuService;
    }

    @Autowired
    public void setMenuService(MenuService menuService) {
        this.menuService = menuService;
    }

    @Autowired
    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
    }
}
