package org.hswebframework.web.authorization.basic.handler.access;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.hswebframework.ezorm.core.param.Term;
import org.hswebframework.utils.ClassUtils;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.access.OwnCreatedDataAccessConfig;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.RecordCreationEntity;
import org.hswebframework.web.commons.entity.param.QueryParamEntity;
import org.hswebframework.web.controller.QueryController;
import org.hswebframework.web.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
public class OwnCreatedDataAccessHandler implements DataAccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(OwnCreatedDataAccessHandler.class);

    @Override
    public boolean isSupport(DataAccessConfig access) {
        return access instanceof OwnCreatedDataAccessConfig;
    }

    @Override
    public boolean handle(DataAccessConfig access, AuthorizingContext context) {
        OwnCreatedDataAccessConfig own = ((OwnCreatedDataAccessConfig) access);
        Object controller = context.getParamContext().getTarget();
        if (controller != null) {
            switch (access.getAction()) {
                case Permission.ACTION_GET:
                case Permission.ACTION_QUERY:
                    return doQueryAccess(own, context);

                case Permission.ACTION_DELETE:
                case Permission.ACTION_UPDATE:
                    return doRWAccess(own, context, controller);
                case Permission.ACTION_ADD:
                    //put creator_id to result
                    return putCreatorId(own, context);
                default:
                    logger.warn("action: {} not support now!", access.getAction());
            }
        } else {
            logger.warn("target is null!");
        }
        return true;
    }

    public boolean putCreatorId(OwnCreatedDataAccessConfig access, AuthorizingContext context) {
        RecordCreationEntity entity = context.getParamContext().getParams()
                .values().stream()
                .filter(RecordCreationEntity.class::isInstance)
                .map(RecordCreationEntity.class::cast)
                .findAny().orElse(null);
        if (entity != null) {
            entity.setCreatorId(context.getAuthentication().getUser().getId());
        } else {
            logger.warn("try put creatorId property,but not found any RecordCreationEntity!");
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected boolean doRWAccess(OwnCreatedDataAccessConfig access, AuthorizingContext context, Object controller) {
        //获取注解
        Object id = context.getParamContext().<String>getParameter(context.getDefinition().getDataAccessDefinition().getIdParameterName()).orElse(null);
        //通过QueryController获取QueryService
        //然后调用selectByPk 查询旧的数据,进行对比
        if (controller instanceof QueryController) {
            //判断是否满足条件(泛型为 RecordCreationEntity)
            Class entityType = ClassUtils.getGenericType(controller.getClass(), 0);
            if (ClassUtils.instanceOf(entityType, RecordCreationEntity.class)) {
                QueryService<RecordCreationEntity, Object> queryService =
                        ((QueryController<RecordCreationEntity, Object, Entity>) controller).getService();
                RecordCreationEntity oldData = queryService.selectByPk(id);
                if (oldData != null && !context.getAuthentication().getUser().getId().equals(oldData.getCreatorId())) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean doQueryAccess(OwnCreatedDataAccessConfig access, AuthorizingContext context) {
        String userId = context.getAuthentication().getUser().getId();

        if (context.getDefinition().getDataAccessDefinition().getPhased() == Phased.before) {
            Entity entity = context.getParamContext().getParams()
                    .values().stream()
                    .filter(Entity.class::isInstance)
                    .map(Entity.class::cast)
                    .findAny().orElse(null);
            if (entity == null) {
                logger.warn("try validate query access, but query entity is null or not instance of org.hswebframework.web.commons.entity.Entity");
                return true;
            }
            if (entity instanceof QueryParamEntity) {
                QueryParamEntity queryParamEntity = ((QueryParamEntity) entity);
                //重构查询条件
                //如: 旧的条件为 where name =? or name = ?
                //重构后为: where creatorId=? and (name = ? or name = ?)
                List<Term> oldParam = queryParamEntity.getTerms();
                //清空旧的查询条件
                queryParamEntity.setTerms(new ArrayList<>());
                //添加一个查询条件
                queryParamEntity
                        .where(RecordCreationEntity.creatorId, userId)
                        //客户端提交的参数 作为嵌套参数
                        .nest().setTerms(oldParam);
            } else if (entity instanceof RecordCreationEntity) {
                ((RecordCreationEntity) entity).setCreatorId(userId);
            } else {
                logger.warn("try validate query access,but entity not support, QueryParamEntity and RecordCreationEntity support now!");
            }
        } else {
            Object result = InvokeResultUtils.convertRealResult(context.getParamContext().getInvokeResult());
            return matchCreatorId(result, userId);
        }
        return true;
    }

    @SuppressWarnings("all")
    protected boolean matchCreatorId(Object result, String userId) {
        if (null == result) {
            return true;
        }
        if (result instanceof RecordCreationEntity) {
            return userId.equals(((RecordCreationEntity) result).getCreatorId());
        } else if (result instanceof Collection) {
            Collection<?> collection = ((Collection) result);
            //删掉不能访问的对象
            collection.removeAll(collection.stream().filter((Object o) -> !matchCreatorId(o, userId)).collect(Collectors.toList()));
        } else {
            try {
                return userId.equals(PropertyUtils.getProperty(result, "creatorId"));
            } catch (Exception ignore) {
            }
        }
        return true;
    }
}
