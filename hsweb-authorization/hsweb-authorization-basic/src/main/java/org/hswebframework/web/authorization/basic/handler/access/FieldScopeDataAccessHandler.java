package org.hswebframework.web.authorization.basic.handler.access;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.ezorm.core.param.TermType;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.access.FieldScopeDataAccessConfig;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author zhouhao
 */
public class FieldScopeDataAccessHandler implements DataAccessHandler {
    private PropertyUtilsBean propertyUtilsBean = BeanUtilsBean.getInstance().getPropertyUtils();

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean isSupport(DataAccessConfig access) {
        return access instanceof FieldScopeDataAccessConfig;
    }

    @Override
    public boolean handle(DataAccessConfig access, AuthorizingContext context) {
        FieldScopeDataAccessConfig own = ((FieldScopeDataAccessConfig) access);
        Object controller = context.getParamContext().getTarget();
        if (controller != null) {
            switch (access.getAction()) {
                case Permission.ACTION_QUERY:
                case Permission.ACTION_GET:
                    return doQueryAccess(own, context);
                case Permission.ACTION_DELETE:
                case Permission.ACTION_UPDATE:
                    return doRWAccess(own, context, controller);
                case Permission.ACTION_ADD:
                default:
                    logger.warn("action: {} not support now!", access.getAction());
            }
        } else {
            logger.warn("target is null!");
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected boolean doRWAccess(FieldScopeDataAccessConfig access, AuthorizingContext context, Object controller) {
        //获取注解
        Object id = context.getParamContext().<String>getParameter(context.getDefinition().getDataAccessDefinition().getIdParameterName()).orElse(null);
        //通过QueryController获取QueryService
        //然后调用selectByPk 查询旧的数据,进行对比
        if (controller instanceof QueryController) {
            QueryService queryService = (QueryService) ((QueryController) controller).getService();
            Object oldData = queryService.selectByPk(id);
            if (oldData != null) {
                try {
                    Object value = propertyUtilsBean.getProperty(oldData, access.getField());
                    return access.getScope().contains(value);
                } catch (Exception e) {
                    logger.error("can't read property {}", access.getField(), e);
                }
                return false;
            }
        } else {
            logger.warn("controller is not instanceof QueryController");
        }
        return true;
    }


    @SuppressWarnings("all")
    protected boolean doQueryAccess(FieldScopeDataAccessConfig access, AuthorizingContext context) {
        if (context.getDefinition().getPhased() == Phased.before) {
            QueryParamEntity entity = context.getParamContext().getParams()
                    .values().stream()
                    .filter(QueryParamEntity.class::isInstance)
                    .map(QueryParamEntity.class::cast)
                    .findAny().orElse(null);
            if (entity == null) {
                logger.warn("try validate query access, but query entity is null or not instance of org.hswebframework.web.commons.entity.Entity");
                return true;
            }
            //重构查询条件
            //如: 旧的条件为 where column =? or column = ?
            //重构后为: where creatorId=? and (column = ? or column = ?)
            List<Term> oldParam = entity.getTerms();
            //清空旧的查询条件
            entity.setTerms(new ArrayList<>());
            //添加一个查询条件
            entity.addTerm(createQueryTerm(access))
                    //客户端提交的参数 作为嵌套参数
                    .nest().setTerms(oldParam);
        } else {
            Object result = InvokeResultUtils.convertRealResult(context.getParamContext().getInvokeResult());
            if (result == null) {
                return true;
            }
            if (result instanceof Collection) {
                return ((Collection) result).stream().allMatch(obj -> propertyInScope(obj, access.getField(), access.getScope()));
            } else {
                return propertyInScope(result, access.getField(), access.getScope());
            }
        }
        return true;
    }

    protected boolean propertyInScope(Object obj, String property, Set<Object> scope) {
        if (null == obj) {
            return false;
        }
        try {
            Object value = BeanUtilsBean.getInstance().getProperty(obj, property);
            if (null != value) {
                return scope.contains(value);
            }
        } catch (Exception ignore) {
            logger.warn("can not get property {} from {},{}", property, obj, ignore.getMessage());
        }
        return true;

    }

    protected Term createQueryTerm(FieldScopeDataAccessConfig access) {
        Term term = new Term();
        term.setType(Term.Type.and);
        term.setColumn(access.getField());
        term.setTermType(TermType.in);
        term.setValue(access.getScope());
        return term;
    }
}
