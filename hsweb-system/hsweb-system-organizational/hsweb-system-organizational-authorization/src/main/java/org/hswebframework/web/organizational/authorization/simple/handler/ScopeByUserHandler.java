package org.hswebframework.web.organizational.authorization.simple.handler;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;
import org.hswebframework.web.organizational.authorization.PersonnelAuthenticationHolder;
import org.hswebframework.web.organizational.authorization.access.*;
import org.hswebframework.web.organizational.authorization.simple.ScopeByUserDataAccessConfig;
import org.hswebframework.web.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;

/**
 * @author zhouhao
 * @since 3.0.5
 */
@Slf4j
@SuppressWarnings("all")
public class ScopeByUserHandler implements DataAccessHandler {

    @Autowired
    private EntityFactory entityFactory;

    @Override
    public boolean isSupport(DataAccessConfig access) {
        return access instanceof ScopeByUserDataAccessConfig;
    }

    @Override
    public boolean handle(DataAccessConfig access, AuthorizingContext context) {
        ScopeByUserDataAccessConfig scope = ((ScopeByUserDataAccessConfig) access);
        switch (access.getAction()) {
            case Permission.ACTION_QUERY:
            case Permission.ACTION_GET:
                return doQueryAccess(scope, context);
            case Permission.ACTION_ADD:
            case Permission.ACTION_UPDATE:
            default:
                return doUpdateAccess(scope, context);
        }
    }

    protected boolean doUpdateAccess(ScopeByUserDataAccessConfig config, AuthorizingContext context) {
        //获取注解
        Object id = context.getParamContext()
                .<String>getParameter(context.getDefinition().getDataAccessDefinition().getIdParameterName())
                .orElse(null);
        if (id == null) {
            return true;
        }
        PersonnelAuthentication authentication = PersonnelAuthentication.current().orElseThrow(UnAuthorizedException::new);
        ScopeInfo scopeInfo = getScope(config, authentication);
        if (scopeInfo.isEmpty()) {
            return false;
        }
        Object controller = context.getParamContext().getTarget();
        QueryService<Object, Object> queryService = null;

        if (controller instanceof QueryController) {
            queryService = ((QueryController<Object, Object, Entity>) controller).getService();
        } else {
            Method getService = ReflectionUtils.findMethod(controller.getClass(), "getService");
            if (getService != null) {
                try {
                    Object service = ReflectionUtils.invokeMethod(getService, controller);
                    if (service instanceof QueryService) {
                        queryService = ((QueryService) service);
                    }
                } catch (Exception ignore) {

                }
            }
        }
        if (queryService != null) {
            ControllerCache controllerCache = getControllerCache(config, context);
            Object entity = queryService.selectByPk(id);
            if (null != entity) {
                String targetId = controllerCache.targetIdGetter.apply(entity);
                log.debug("执行数据权限控制,scope:{},target:{}", scopeInfo.scope, targetId);
                if (targetId == null) {
                    return true;
                }
                return scopeInfo.allScope.contains(controllerCache.targetIdGetter.apply(entity));
            }
        } else {
            log.warn("Controller没有实现任何通用CURD功能,无法进行数据权限控制!");
        }
        return true;

    }

    private ScopeInfo getScope(ScopeByUserDataAccessConfig config, PersonnelAuthentication authentication) {
        String termType = null;
        Set<String> scope = null, allScope = null;
        ScopeInfo scopeInfo = new ScopeInfo();
        if (authentication == null) {
            return scopeInfo;
        }
        Consumer<Query<?, QueryParamEntity>> consumer;

        switch (config.getScopeType()) {
            case DataAccessType.ORG_SCOPE:
                termType = "user-in-org";
                scope = authentication.getRootOrgId();
                allScope = config.isChildren() ? authentication.getAllOrgId() : scope;
                break;
            case DataAccessType.DEPARTMENT_SCOPE:
                termType = "user-in-department";
                scope = authentication.getRootDepartmentId();
                allScope = config.isChildren() ? authentication.getAllDepartmentId() : scope;
                break;
            case DataAccessType.POSITION_SCOPE:
                termType = "user-in-position";
                scope = authentication.getRootPositionId();
                allScope = config.isChildren() ? authentication.getAllPositionId() : scope;
                break;
            case DataAccessType.DISTRICT_SCOPE:
                termType = "user-in-dist";
                scope = authentication.getRootDistrictId();
                allScope = config.isChildren() ? authentication.getAllDistrictId() : scope;
                break;
            case "CUSTOM_SCOPE_ORG":
                termType = "user-in-org";
                scope = config.getScope();
                allScope = scope;
                break;
            case "CUSTOM_SCOPE_DEPT":
                termType = "user-in-department";
                scope = config.getScope();
                allScope = scope;
                break;
            case "CUSTOM_SCOPE_DIST":
                termType = "user-in-dist";
                scope = config.getScope();
                allScope = scope;
                break;
            default:
                log.warn("不支持的数据权限范围:{}", config.getScopeType());
        }
        if (termType == null) {
            return scopeInfo;
        }
        scopeInfo.scope = new ArrayList<>(scope);
        scopeInfo.allScope = new ArrayList<>(allScope);
        scopeInfo.termType = termType;
        if (config.isChildren()) {
            scopeInfo.termType = termType + termType.concat("-child");
        }
        return scopeInfo;

    }

    class ScopeInfo {
        String termType;

        List<String> scope;
        List<String> allScope;

        Consumer<Query<?, QueryParamEntity>> notUserConsumer;

        public boolean isEmpty() {
            return termType == null || scope == null || scope.isEmpty();
        }
    }

    static Function<Object, String> defaultTargetIdGetter = entity -> {
        Map<String, String> userInfo = FastBeanCopier.copy(entity, new HashMap<>(),
                FastBeanCopier.include("creatorId"));
        return userInfo.get("creatorId");
    };

    static BiConsumer<Query<?, QueryParamEntity>, ScopeInfo> defaultQueryConsumer = (query, scopeInfo) -> {
        query.and("creatorId", scopeInfo.termType, scopeInfo.scope);
    };
    static Function<AuthorizingContext, Set<String>>         defaultScopeGetter   = context -> {
        return Collections.singleton(context.getAuthentication().getUser().getId());
    };

    private <E> Function<Object, String> createGetter(Class<E> type, Function<E, String> getter) {
        return entity -> {
            if (type.isInstance(entity)) {
                return getter.apply(((E) entity));
            }
            return defaultTargetIdGetter.apply(entity);
        };
    }

    static Map<Class, String> cache = new ConcurrentHashMap<>();

    protected <T> String getControlProperty(Class type, Function<T, String> function) {
        return cache.computeIfAbsent(type, t -> {
            return function.apply((T) entityFactory.newInstance(type));
        });
    }

    class ControllerCache {
        Function<Object, String> targetIdGetter = defaultTargetIdGetter;

        BiConsumer<Query<?, QueryParamEntity>, ScopeInfo> queryConsumer = defaultQueryConsumer;

        Function<AuthorizingContext, Set<String>> scopeGetter = defaultScopeGetter;
    }

    @EqualsAndHashCode
    @Getter
    @Setter
    class CacheKey {
        private String className;

        private boolean children;

        private String type;
    }

    static Map<CacheKey, ControllerCache> cacheMap = new ConcurrentHashMap<>();


    private ControllerCache getControllerCache(ScopeByUserDataAccessConfig config, AuthorizingContext context) {
        CacheKey cacheKey = new CacheKey();
        cacheKey.children = config.isChildren();
        cacheKey.className = ClassUtils.getUserClass(context.getParamContext().getTarget().getClass()).getName();
        cacheKey.type = config.getScopeType();
        return cacheMap.computeIfAbsent(cacheKey, key -> {
            ControllerCache controllerCache = new ControllerCache();
            if (context.getParamContext().getTarget() instanceof QueryController) {
                boolean children = config.isChildren();
                Class controller = ClassUtils.getUserClass(context.getParamContext().getTarget().getClass());
                Class entityClass = org.hswebframework.utils.ClassUtils.getGenericType(controller, 0);
                if (RecordCreationEntity.class.isAssignableFrom(entityClass)) {
                    controllerCache.targetIdGetter = createGetter(RecordCreationEntity.class, RecordCreationEntity::getCreatorId);
                    controllerCache.queryConsumer = (query, scopeInfo) -> {
                        query.in(getControlProperty(entityClass, RecordCreationEntity::getCreatorIdProperty), scopeInfo.termType, scopeInfo.scope);
                    };
                } else if (OrgAttachEntity.class.isAssignableFrom(entityClass) && config.getScopeType().contains("ORG")) {
                    controllerCache.targetIdGetter = createGetter(OrgAttachEntity.class, OrgAttachEntity::getOrgId);
                    controllerCache.queryConsumer = (query, scopeInfo) -> {
                        query.and(getControlProperty(entityClass, OrgAttachEntity::getOrgIdProperty), children ? "org-child-in" : "in", scopeInfo.scope);
                    };
                } else if (DepartmentAttachEntity.class.isAssignableFrom(entityClass) && config.getScopeType().contains("DEPT")) {
                    controllerCache.targetIdGetter = createGetter(DepartmentAttachEntity.class, DepartmentAttachEntity::getDepartmentId);
                    controllerCache.queryConsumer = (query, scopeInfo) -> {
                        query.and(getControlProperty(entityClass, DepartmentAttachEntity::getDepartmentIdProperty), children ? "dept-child-in" : "in", scopeInfo.scope);
                    };
                } else if (PositionAttachEntity.class.isAssignableFrom(entityClass) && config.getScopeType().contains("POS")) {
                    controllerCache.targetIdGetter = createGetter(PositionAttachEntity.class, PositionAttachEntity::getPositionId);
                    controllerCache.queryConsumer = (query, scopeInfo) -> {
                        query.and(getControlProperty(entityClass, PositionAttachEntity::getPositionIdProperty), children ? "pos-child-in" : "in", scopeInfo.scope);
                    };
                } else if (DistrictAttachEntity.class.isAssignableFrom(entityClass) && config.getScopeType().contains("DIST")) {
                    controllerCache.targetIdGetter = createGetter(DistrictAttachEntity.class, DistrictAttachEntity::getDistrictId);
                    controllerCache.queryConsumer = (query, scopeInfo) -> {
                        query.and(getControlProperty(entityClass, DistrictAttachEntity::getDistrictIdProperty), children ? "dist-child-in" : "in", scopeInfo.scope);
                    };
                } else {
                    String userIdField = getUserField(entityClass);
                    controllerCache.targetIdGetter = entity -> {
                        Map<String, String> userInfo = FastBeanCopier.copy(entity, new HashMap<>(),
                                FastBeanCopier.include(userIdField));
                        return userInfo.get(userIdField);
                    };
                    controllerCache.queryConsumer = (query, scopeInfo) -> {
                        query.and(userIdField, scopeInfo.termType, scopeInfo.scope);
                    };
                }
            }
            return controllerCache;
        });
    }

    protected boolean doQueryAccess(ScopeByUserDataAccessConfig config, AuthorizingContext context) {
        PersonnelAuthentication authentication = PersonnelAuthentication
                .current()
                .orElseThrow(AccessDenyException::new);
        ScopeInfo scopeInfo = getScope(config, authentication);
        if (scopeInfo.isEmpty()) {
            return false;
        }
        ControllerCache controllerCache = getControllerCache(config, context);

        //如果是执行后
        if (context.getDefinition().getDataAccessDefinition().getPhased() == Phased.after) {
            Object result = context.getParamContext().getInvokeResult();
            if (result == null) {
                return true;
            }
            if (result instanceof ResponseEntity) {
                result = ((ResponseEntity) result).getBody();
            }
            if (result instanceof ResponseMessage) {
                result = ((ResponseMessage) result).getResult();
            }
            String value = controllerCache.targetIdGetter.apply(result);
            log.debug("执行数据权限控制[{}],scope:{},target:{}", config.getScopeTypeName(), scopeInfo.scope, value);
            if (value == null) {
                return true;
            }
            return scopeInfo.allScope.contains(value);
        }

        Entity entity = context.getParamContext()
                .getParams()
                .values()
                .stream()
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .findAny()
                .orElse(null);

        if (entity instanceof QueryParamEntity) {
            QueryParamEntity param = ((QueryParamEntity) entity);
            param.toNestQuery(query -> {
                log.debug("执行查询数据权限控制[{}],scope:{}", config.getScopeTypeName(), scopeInfo.scope);
                controllerCache.queryConsumer.accept(query, scopeInfo);
            });
        }
        return true;
    }

    public String getUserField(Class type) {

        if (ReflectionUtils.findField(type, "userId") != null) {
            return "userId";
        }

        return "creatorId";
    }
}
