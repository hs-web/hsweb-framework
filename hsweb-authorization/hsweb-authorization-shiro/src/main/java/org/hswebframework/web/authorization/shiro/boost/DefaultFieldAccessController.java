package org.hswebframework.web.authorization.shiro.boost;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.FieldAccessConfig;
import org.hswebframework.web.authorization.access.FieldAccessController;
import org.hswebframework.web.boost.aop.context.MethodInterceptorParamContext;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.commons.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * 默认的字段级权限控制,目前已实现提供对查询(query),更新(update)的权限控制。
 * 控制方式主要是通过被拦截方法的参数类型进行识别,如果是可进行控制的参数类型,则通过修改参数属性等方式,进行控制。
 *
 * @author zhouhao
 * @see FieldAccessController
 * @since 3.0
 */
public class DefaultFieldAccessController implements FieldAccessController {

    private Logger logger = LoggerFactory.getLogger(DefaultFieldAccessController.class);

    @Override
    public boolean doAccess(String action, Set<FieldAccessConfig> accesses, MethodInterceptorParamContext params) {
        //控制转发
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

    /**
     * 执行更新操作的控制,此方法永远返回true.通过取得参数中实现{@link Entity}的参数,将把这个参数实体所对应不能操作的字段全部设置为null。 <br>
     * 注意: 此方式还需要dao框架的支持(为null的字段不进行更新) <br>
     * 如果没有{@link Entity}的参数,则不进行控制并给出警告信息
     *
     * @param accesses 不可操作的字段
     * @param params   参数上下文
     * @return true
     * @see BeanUtilsBean
     * @see org.apache.commons.beanutils.PropertyUtilsBean
     */
    protected boolean doUpdateAccess(Set<FieldAccessConfig> accesses, MethodInterceptorParamContext params) {
        Object supportParam = params.getParams().values().stream()
                .filter(param -> (param instanceof Entity) | (param instanceof Model))
                .findAny().orElse(null);
        if (null != supportParam) {
            for (FieldAccessConfig access : accesses) {
                try {
                    //设置值为null,跳过修改
                    BeanUtilsBean.getInstance()
                            .getPropertyUtils()
                            .setProperty(supportParam, access.getField(), null);
                } catch (Exception e) {
                }
            }
            if (supportParam instanceof RecordCreationEntity) {
                RecordCreationEntity creationEntity = ((RecordCreationEntity) supportParam);
                creationEntity.setCreateTime(null);
                creationEntity.setCreatorId(null);
            }
        } else {
            logger.warn("doUpdateAccess skip ,because can not found any entity in param!");
        }
        return true;
    }

    /**
     * 执行查询的控制,查询主要针对参数为{@link QueryParamEntity}的动态条件查询,通过设置{@link QueryParamEntity#excludes(String...)}.指定不需要查询的字段
     * 如果没有{@link QueryParamEntity}的参数,则不进行控制并给出警告信息
     *
     * @param accesses 不能查询的字段
     * @param params   参数上下文
     * @return true
     */
    protected boolean doQueryAccess(Set<FieldAccessConfig> accesses, MethodInterceptorParamContext params) {
        QueryParamEntity paramEntity = params.getParams().values().stream()
                .filter(QueryParamEntity.class::isInstance)
                .map(QueryParamEntity.class::cast)
                .findAny().orElse(null);
        if (paramEntity != null) {
            paramEntity.excludes(accesses.stream().map(FieldAccessConfig::getField).toArray(String[]::new));
        } else {
            logger.warn("doQueryAccess skip ,because can not found any QueryParamEntity in param!");
        }
        return true;
    }
}
