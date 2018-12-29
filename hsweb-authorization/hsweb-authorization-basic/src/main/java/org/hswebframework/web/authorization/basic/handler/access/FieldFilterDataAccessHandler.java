package org.hswebframework.web.authorization.basic.handler.access;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.access.FieldFilterDataAccessConfig;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.model.Model;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据权限字段过滤处理,目前仅支持deny. {@link DataAccessConfig.DefaultType#DENY_FIELDS}
 *
 * @author zhouhao
 */
public class FieldFilterDataAccessHandler implements DataAccessHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public boolean isSupport(DataAccessConfig access) {
        return access instanceof FieldFilterDataAccessConfig;
    }

    @Override
    public boolean handle(DataAccessConfig access, AuthorizingContext context) {
        FieldFilterDataAccessConfig filterDataAccessConfig = ((FieldFilterDataAccessConfig) access);

        switch (access.getAction()) {
            case Permission.ACTION_QUERY:
            case Permission.ACTION_GET:
                return doQueryAccess(filterDataAccessConfig, context);
            case Permission.ACTION_ADD:
            case Permission.ACTION_UPDATE:
                return doUpdateAccess(filterDataAccessConfig, context);
            default:
                if (logger.isDebugEnabled()) {
                    logger.debug("field filter not support for {}", access.getAction());
                }
                return true;
        }
    }

    /**
     * @param accesses 不可操作的字段
     * @param params   参数上下文
     * @return true
     * @see BeanUtilsBean
     * @see org.apache.commons.beanutils.PropertyUtilsBean
     */
    protected boolean doUpdateAccess(FieldFilterDataAccessConfig accesses, AuthorizingContext params) {
        Map<String, Object> paramsMap = params.getParamContext().getParams();

        Object supportParam = paramsMap.size() == 0 ?
                paramsMap.values().iterator().next() :
                paramsMap.values().stream()
                        .filter(param -> (param instanceof Entity) || (param instanceof Model) || (param instanceof Map))
                        .findAny()
                        .orElse(null);
        if (null != supportParam) {
            for (String field : accesses.getFields()) {
                try {
                    //设置值为null,跳过修改
                    BeanUtilsBean.getInstance()
                            .getPropertyUtils()
                            .setProperty(supportParam, field, null);
                } catch (Exception e) {
                    logger.warn("can't set {} null", field, e);
                }
            }
        } else {
            logger.warn("doUpdateAccess skip ,because can not found any support entity in param!");
        }
        return true;
    }

    @SuppressWarnings("all")
    protected boolean doQueryAccess(FieldFilterDataAccessConfig access, AuthorizingContext context) {
        if (context.getDefinition().getDataAccessDefinition().getPhased() == Phased.before) {
            QueryParamEntity entity = context.getParamContext().getParams()
                    .values().stream()
                    .filter(QueryParamEntity.class::isInstance)
                    .map(QueryParamEntity.class::cast)
                    .findAny().orElse(null);
            if (entity == null) {
                logger.warn("try validate query access, but query entity is null or not instance of org.hswebframework.web.commons.entity.Entity");
                return true;
            }
            Set<String> denyFields = access.getFields();
            entity.excludes(denyFields.toArray(new String[denyFields.size()]));
        } else {
            Object result = InvokeResultUtils.convertRealResult(context.getParamContext().getInvokeResult());
            if (result instanceof Collection) {
                ((Collection) result).forEach(o -> setObjectPropertyNull(o, access.getFields()));
            } else {
                setObjectPropertyNull(result, access.getFields());
            }
        }
        return true;
    }

    protected void setObjectPropertyNull(Object obj, Set<String> fields) {
        if (null == obj) {
            return;
        }
        for (String field : fields) {
            try {
                BeanUtilsBean.getInstance().getPropertyUtils().setProperty(obj, field, null);
            } catch (Exception ignore) {

            }
        }
    }
}
