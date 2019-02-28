package org.hswebframework.web.organizational.authorization.simple.handler;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.dsl.Query;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.User;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.access.UserAttachEntity;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.exception.UnAuthorizedException;
import org.hswebframework.web.bean.FastBeanCopier;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.commons.entity.RecordCreationEntity;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.organizational.authorization.PersonnelAuthentication;
import org.hswebframework.web.organizational.authorization.access.*;
import org.hswebframework.web.organizational.authorization.simple.ScopeByUserDataAccessConfig;
import org.hswebframework.web.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
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
        //获取id参数
        Object id = context.getParamContext()
                .<String>getParameter(context.getDefinition().getDataAccessDefinition().getIdParameterName())
                .orElse(null);
        if (id == null) {
            return true;
        }
        PersonnelAuthentication authentication = PersonnelAuthentication
                .current().orElse(null);
        if (authentication == null) {
            log.warn("当前用户没有关联人员信息!");
            return false;
        }
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
                if (targetId == null) {
                    return true;
                }
                log.debug("执行数据权限控制,范围:{},结果:{}", scopeInfo.scope, targetId);
                return scopeInfo.allScope.contains(controllerCache.targetIdGetter.apply(entity));
            }
        } else {
            log.debug("Controller没有实现任何通用CURD功能,无法进行数据权限控制!");
        }
        return true;

    }

    @SneakyThrows
    private ScopeInfo getScope(ScopeByUserDataAccessConfig config, PersonnelAuthentication authentication) {
        String termType = null, personTermType = "in";
        Set<String> scope = null, allScope = null;
        ScopeInfo scopeInfo = new ScopeInfo();
        if (authentication == null) {
            return scopeInfo;
        }
        Consumer<Query<?, QueryParamEntity>> consumer;

        switch (config.getScopeType()) {
            case "OWN_PERSON":
                termType = "in";
                scope = Collections.singleton(authentication.getPersonnel().getId());
                allScope = scope;
                break;
            case "OWN_USER":
                termType = "in";
                scope = Collections.singleton(Authentication
                        .current()
                        .map(Authentication::getUser)
                        .map(User::getId)
                        .orElseThrow(AccessDenyException::new));
                allScope = scope;
                break;
            case DataAccessType.ORG_SCOPE:
                termType = "user-in-org";
                personTermType = "person-in-org";
                scope = authentication.getRootOrgId();
                allScope = config.isChildren() ? authentication.getAllOrgId() : scope;
                break;
            case DataAccessType.DEPARTMENT_SCOPE:
                termType = "user-in-department";
                personTermType = "person-in-department";
                scope = authentication.getRootDepartmentId();
                allScope = config.isChildren() ? authentication.getAllDepartmentId() : scope;
                break;
            case DataAccessType.POSITION_SCOPE:
                termType = "user-in-position";
                personTermType = "person-in-position";
                scope = authentication.getRootPositionId();
                allScope = config.isChildren() ? authentication.getAllPositionId() : scope;
                break;
            case DataAccessType.DISTRICT_SCOPE:
                termType = "user-in-dist";
                personTermType = "person-in-dist";
                scope = authentication.getRootDistrictId();
                allScope = config.isChildren() ? authentication.getAllDistrictId() : scope;
                break;
            case "CUSTOM_SCOPE_ORG":
                termType = "user-in-org";
                personTermType = "person-in-org";
                scope = config.getScope();
                allScope = scope;
                break;
            case "CUSTOM_SCOPE_POSITION":
                termType = "user-in-position";
                personTermType = "person-in-position";
                scope = config.getScope();
                allScope = scope;
                break;
            case "CUSTOM_SCOPE_DEPT":
                termType = "user-in-department";
                personTermType = "person-in-department";
                scope = config.getScope();
                allScope = scope;
                break;
            case "CUSTOM_SCOPE_DIST":
                termType = "user-in-dist";
                personTermType = "person-in-dist";
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
            if (!termType.equals("in")) {
                scopeInfo.termType = termType.concat("-child");
            }
            if (!personTermType.equals("in")) {
                scopeInfo.personTermType = personTermType.concat("-child");
            }
        }
        return scopeInfo;

    }

    class ScopeInfo {
        String termType;
        String personTermType;

        List<String> scope;
        List<String> allScope;

        Consumer<Query<?, QueryParamEntity>> notUserConsumer;

        public boolean isEmpty() {
            return termType == null || scope == null || scope.isEmpty();
        }
    }

    static Function<Object, String> defaultTargetIdGetter = entity -> {
        Map<String, String> userInfo = FastBeanCopier.copy(entity, new HashMap<>(), FastBeanCopier.include("creatorId", "userId"));
        return userInfo.getOrDefault("userId", userInfo.get("creatorId"));
    };

    protected Function<Object, String> createTargetIdGetter(Class entityClass, String... properties) {
        String useProperty = null;
        for (String property : properties) {
            Field field = ReflectionUtils.findField(entityClass, property);
            if (field != null) {
                useProperty = property;
            }
        }
        if (useProperty == null) {
            log.debug("类[{}]中未包含字段[{}],可能无法进行数据权限控制.", entityClass, Arrays.asList(properties));
        }
        return entity -> {
            Map<String, String> userInfo = FastBeanCopier.copy(entity, new HashMap<>(), FastBeanCopier.include(properties));
            for (String property : properties) {
                String value = userInfo.get(property);
                if (value != null) {
                    return value;
                }
            }
            return null;
        };
    }

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

   // static Map<Class, String> cache = new ConcurrentHashMap<>();

    protected <T> String getControlProperty(Class type, Function<T, String> function) {
        return function.apply((T) entityFactory.newInstance(type));
//        return cache.computeIfAbsent(type, t -> {
//            return function.apply((T) entityFactory.newInstance(type));
//        });
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

        private String method;

        private boolean children;

        private String type;

        private boolean queryController;
    }

    static Map<CacheKey, ControllerCache> cacheMap = new ConcurrentHashMap<>();


    private ControllerCache getControllerCache(ScopeByUserDataAccessConfig config, AuthorizingContext context) {
        Class controller = ClassUtils.getUserClass(context.getParamContext().getTarget().getClass());
        CacheKey cacheKey = new CacheKey();
        cacheKey.children = config.isChildren();
        cacheKey.className = controller.getName();
        cacheKey.method = context.getParamContext().getMethod().toString();
        cacheKey.type = config.getScopeType();
        cacheKey.queryController = context.getParamContext().getTarget() instanceof QueryController;
        Class dataAccessEntityType = context.getDefinition().getDataAccessDefinition().getEntityType();

        return cacheMap.computeIfAbsent(cacheKey, key -> {
            ControllerCache controllerCache = new ControllerCache();
            Class entityClass = dataAccessEntityType;
            if (entityClass == Void.class) {
                if (key.queryController) {
                    entityClass = org.hswebframework.utils.ClassUtils.getGenericType(controller, 0);
                }
            }
            boolean children = key.isChildren();
            //控制机构
            if (key.getType().contains("ORG") && OrgAttachEntity.class.isAssignableFrom(entityClass)) {
                String property = getControlProperty(entityClass, OrgAttachEntity::getOrgIdProperty);
                controllerCache.targetIdGetter = createGetter(OrgAttachEntity.class, OrgAttachEntity::getOrgId);
                controllerCache.queryConsumer = (query, scopeInfo) -> {
                    query.and(property, children ? "org-child-in" : "in", scopeInfo.scope);
                };
                //部门
            } else if (key.getType().contains("DEPT") && DepartmentAttachEntity.class.isAssignableFrom(entityClass)) {
                String property = getControlProperty(entityClass, DepartmentAttachEntity::getDepartmentIdProperty);
                controllerCache.targetIdGetter = createGetter(DepartmentAttachEntity.class, DepartmentAttachEntity::getDepartmentId);
                controllerCache.queryConsumer = (query, scopeInfo) -> {
                    query.and(property, children ? "org-child-in" : "in", scopeInfo.scope);
                };
                //岗位
            } else if (key.getType().contains("POS") && PositionAttachEntity.class.isAssignableFrom(entityClass)) {
                String property = getControlProperty(entityClass, PositionAttachEntity::getPositionIdProperty);
                controllerCache.targetIdGetter = createGetter(PositionAttachEntity.class, PositionAttachEntity::getPositionId);
                controllerCache.queryConsumer = (query, scopeInfo) -> {
                    query.and(property, children ? "pos-child-in" : "in", scopeInfo.scope);
                };
                //行政区划
            } else if (key.getType().contains("DIST") && DistrictAttachEntity.class.isAssignableFrom(entityClass)) {
                String property = getControlProperty(entityClass, DistrictAttachEntity::getDistrictIdProperty);
                controllerCache.targetIdGetter = createGetter(DistrictAttachEntity.class, DistrictAttachEntity::getDistrictId);
                controllerCache.queryConsumer = (query, scopeInfo) -> {
                    query.and(property, children ? "dist-child-in" : "in", scopeInfo.scope);
                };
                //人员
            } else if (key.getType().contains("PERSON") && PersonAttachEntity.class.isAssignableFrom(entityClass)) {
                String property = getControlProperty(entityClass, PersonAttachEntity::getPersonIdProperty);
                controllerCache.targetIdGetter = createGetter(PersonAttachEntity.class, PersonAttachEntity::getPersonId);
                controllerCache.queryConsumer = (query, scopeInfo) -> {
                    query.and(property, scopeInfo.termType, scopeInfo.scope);
                };
                //根据用户控制
            } else {
                if (UserAttachEntity.class.isAssignableFrom(entityClass)) {
                    String property = getControlProperty(entityClass, UserAttachEntity::getUserIdProperty);
                    controllerCache.targetIdGetter = createGetter(UserAttachEntity.class, UserAttachEntity::getUserId);
                    controllerCache.queryConsumer = (query, scopeInfo) -> {
                        query.and(property, scopeInfo.termType, scopeInfo.scope);
                    };
                } else if (RecordCreationEntity.class.isAssignableFrom(entityClass)) {
                    String property = getControlProperty(entityClass, RecordCreationEntity::getCreatorIdProperty);
                    controllerCache.targetIdGetter = createGetter(RecordCreationEntity.class, RecordCreationEntity::getCreatorId);
                    controllerCache.queryConsumer = (query, scopeInfo) -> {
                        query.and(property, scopeInfo.termType, scopeInfo.scope);
                    };
                } else {
                    String property = getUserField(entityClass);
                    controllerCache.targetIdGetter = createTargetIdGetter(entityClass, property);
                    controllerCache.queryConsumer = (query, scopeInfo) -> {
                        query.and(property, scopeInfo.termType, scopeInfo.scope);
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
            result = getRealResult(result);
            Predicate<Object> predicate = (o) -> {
                String value = controllerCache.targetIdGetter.apply(o);
                if (value == null) {
                    return true;
                }
                log.debug("执行数据权限控制[{}],scope:{},target:{}", config.getScopeTypeName(), scopeInfo.scope, value);
                return scopeInfo.allScope.contains(value);
            };
            if (result instanceof Collection) {
                Collection<?> res = ((Collection) result);
                if (res.isEmpty()) {
                    return true;
                }
                return res.stream().allMatch(predicate);
            } else {
                return predicate.test(result);
            }
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
                log.debug("执行查询数据权限控制[{}],范围:{}", config.getScopeTypeName(), scopeInfo.scope);
                controllerCache.queryConsumer.accept(query, scopeInfo);
            });
        } else {
            log.debug("方法[{}]未使用动态查询参数[QueryParamEntity],无法进行数据权限控制!", context.getParamContext().getMethod());
        }
        return true;
    }

    public String getUserField(Class type) {

        if (ReflectionUtils.findField(type, "userId") != null) {
            return "userId";
        }

        return "creatorId";
    }

    protected Object getRealResult(Object result) {
        if (result instanceof ResponseEntity) {
            result = ((ResponseEntity) result).getBody();
        }
        if (result instanceof ResponseMessage) {
            result = ((ResponseMessage) result).getResult();
        }
        if (result instanceof PagerResult) {
            result = ((PagerResult) result).getData();
        }
        return result;
    }
}
