package org.hswebframework.web.organizational.authorization.simple.handler;

import org.hsweb.ezorm.core.param.Term;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.access.ScopeDataAccessConfig;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.boost.aop.context.MethodInterceptorParamContext;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.organizational.authorization.PersonnelAuthorization;
import org.hswebframework.web.organizational.authorization.access.DataAccessType;
import org.hswebframework.web.organizational.authorization.entity.OrgAttachEntity;
import org.hswebframework.web.service.QueryService;
import org.hswebframwork.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public abstract class AbstractScopeDataAccessHandler<E> implements DataAccessHandler {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private boolean defaultSuccessOnError = true;

    protected abstract Class<E> getEntityClass();

    protected abstract String getSupportScope();

    protected abstract String getOperationScope(E entity);

    protected abstract Term createQueryTerm(Set<String> scope);

    protected abstract Set<String> getTryOperationScope(String scopeType, PersonnelAuthorization authorization);

    @Override
    public boolean isSupport(DataAccessConfig access) {
        return access instanceof ScopeDataAccessConfig && access.getType().equals(getSupportScope());
    }

    @Override
    public boolean handle(DataAccessConfig access, MethodInterceptorParamContext context) {
        ScopeDataAccessConfig accessConfig = ((ScopeDataAccessConfig) access);
        switch (accessConfig.getAction()) {
            case Permission.ACTION_QUERY:
                return handleQuery(accessConfig, context);
            case Permission.ACTION_GET:
            case Permission.ACTION_DELETE:
            case Permission.ACTION_UPDATE:
                return handleRW(accessConfig, context);
            case Permission.ACTION_ADD:
                return handleAdd(accessConfig, context);
        }
        return false;
    }

    protected PersonnelAuthorization getPersonnelAuthorization() {
        return PersonnelAuthorization.current()
                .orElseThrow(UnsupportedOperationException::new); // TODO: 17-5-23 其他异常?
    }

    protected boolean handleAdd(ScopeDataAccessConfig access, MethodInterceptorParamContext context) {
        PersonnelAuthorization authorization = getPersonnelAuthorization();
        Set<String> scopes = authorization.getRootOrgId();
        String scope = null;
        if (scopes.size() == 0) return true;
        else if (scopes.size() == 1) scope = scopes.iterator().next();
        else logger.warn("existing many scope :{} , try use config.", scopes);
        scopes = access.getScope().stream().map(String::valueOf).collect(Collectors.toSet());
        if (scope == null && scopes.size() == 1) {
            scope = scopes.iterator().next();
        }
        if (scope != null) {
            String finalScopeId = scope;
            context.getParams().values().stream()
                    .filter(OrgAttachEntity.class::isInstance)
                    .map(OrgAttachEntity.class::cast)
                    .forEach(entity -> entity.setOrgId(finalScopeId));
        } else {
            logger.warn("scope is null!");
        }
        return defaultSuccessOnError;
    }

    protected boolean handleRW(ScopeDataAccessConfig access, MethodInterceptorParamContext context) {
        //获取注解
        RequiresDataAccess dataAccess = context.getAnnotation(RequiresDataAccess.class);
        Object id = context.<String>getParameter(dataAccess.idParamName()).orElse(null);
        Object controller = context.getTarget();
        Set<String> ids = getTryOperationScope(access);
        String errorMsg;
        //通过QueryController获取QueryService
        //然后调用selectByPk 查询旧的数据,进行对比
        if (controller instanceof QueryController) {
            //判断是否满足条件(泛型为 getEntityClass)
            Class entityType = ClassUtils.getGenericType(controller.getClass(), 0);
            if (ClassUtils.instanceOf(entityType, getEntityClass())) {
                QueryService<E, Object> queryService =
                        ((QueryController<E, Object, Entity>) controller).getService();
                E oldData = queryService.selectByPk(id);
                if (oldData != null && ids.contains(getOperationScope(oldData))) {
                    return false;
                } else {
                    return true;
                }
            } else {
                errorMsg = "GenericType[0] not instance of " + getEntityClass();
            }
        } else {
            errorMsg = "target not instance of QueryController";
        }
        logger.warn("do handle {} fail,because {}", access.getAction(), errorMsg);
        return defaultSuccessOnError;
    }

    protected Set<String> getTryOperationScope(ScopeDataAccessConfig access) {
        if (DataAccessType.SCOPE_TYPE_CUSTOM.equals(access.getScopeType()))
            return access.getScope().stream().map(String::valueOf).collect(Collectors.toSet());
        return getTryOperationScope(access.getScopeType(), getPersonnelAuthorization());
    }

    protected boolean handleQuery(ScopeDataAccessConfig access, MethodInterceptorParamContext context) {
        Entity entity = context.getParams()
                .values().stream()
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .findAny().orElse(null);
        if (entity == null) {
            logger.warn("try validate query access, but query entity is null or not instance of org.hswebframework.web.commons.entity.Entity");
            return defaultSuccessOnError;
        }
        Set<String> scope = getTryOperationScope(access);
        if (scope.isEmpty()) {
            logger.warn("try validate query access,but config is empty!");
            return defaultSuccessOnError;
        }
        if (entity instanceof QueryParamEntity) {
            if (logger.isDebugEnabled())
                logger.debug("try rebuild query param ...");
            QueryParamEntity queryParamEntity = ((QueryParamEntity) entity);
            //重构查询条件
            //如: 旧的条件为 where name =? or name = ?
            //重构后为: where org_id in(?,?) and (name = ? or name = ?)
            List<Term> oldParam = queryParamEntity.getTerms();
            //清空旧的查询条件
            queryParamEntity.setTerms(new ArrayList<>());
            //添加一个查询条件
            queryParamEntity
                    .addTerm(createQueryTerm(scope))
                    //客户端提交的参数 作为嵌套参数
                    .nest().setTerms(oldParam);
        } else {
            logger.warn("try validate query access,but entity not support, QueryParamEntity support now!");
        }
        return true;
    }
}
