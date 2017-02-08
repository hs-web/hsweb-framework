package org.hswebframework.web.authorization.shiro.boost;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.FieldAccess;
import org.hswebframework.web.authorization.access.FieldAccessController;
import org.hswebframework.web.authorization.access.ParamContext;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class DefaultFieldAccessController implements FieldAccessController {

    private Logger logger = LoggerFactory.getLogger(DefaultFieldAccessController.class);

    @Override
    public boolean doAccess(String action, Set<FieldAccess> accesses, ParamContext params) {
        switch (action) {
            case Permission.ACTION_QUERY:
                return doQueryAccess(accesses, params);
            case Permission.ACTION_UPDATE:
                return doUpdateAccess(accesses, params);
            default:
                logger.warn("action {} not support now!", action);
        }
        return false;
    }

    protected boolean doUpdateAccess(Set<FieldAccess> accesses, ParamContext params) {
        Entity entity = params.getParams().values().stream()
                .filter(Entity.class::isInstance)
                .map(Entity.class::cast)
                .findAny().orElse(null);
        if (null != entity) {
            for (FieldAccess access : accesses) {
                try {
                    //设置值为null,跳过修改
                    BeanUtilsBean.getInstance()
                            .getPropertyUtils()
                            .setProperty(entity, access.getField(), null);
                } catch (Exception e) {
                }
            }
        } else {
            logger.warn("doUpdateAccess skip ,because can not found any entity in param!");
        }
        return true;
    }

    protected boolean doQueryAccess(Set<FieldAccess> accesses, ParamContext params) {
        QueryParamEntity paramEntity = params.getParams().values().stream()
                .filter(QueryParamEntity.class::isInstance)
                .map(QueryParamEntity.class::cast)
                .findAny().orElse(null);
        if (paramEntity != null) {
            paramEntity.excludes(accesses.stream().map(FieldAccess::getField).toArray(String[]::new));
        } else {
            logger.warn("doQueryAccess skip ,because can not found any QueryParamEntity in param!");
        }
        return true;
    }
}
