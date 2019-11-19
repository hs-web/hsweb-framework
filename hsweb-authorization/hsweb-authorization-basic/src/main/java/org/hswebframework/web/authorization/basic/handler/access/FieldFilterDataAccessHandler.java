package org.hswebframework.web.authorization.basic.handler.access;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessHandler;
import org.hswebframework.web.authorization.access.FieldFilterDataAccessConfig;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.define.Phased;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
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
            case Permission.ACTION_SAVE:
            case Permission.ACTION_UPDATE:
                return doUpdateAccess(filterDataAccessConfig, context);
            default:
                if (logger.isDebugEnabled()) {
                    logger.debug("field filter not support for {}", access.getAction());
                }
                return true;
        }
    }

    protected void applyUpdateParam(FieldFilterDataAccessConfig config, Object... parameter) {

        for (Object data : parameter) {
            for (String field : config.getFields()) {
                try {
                    //设置值为null,跳过修改
                    BeanUtilsBean.getInstance()
                            .getPropertyUtils()
                            .setProperty(data, field, null);
                } catch (Exception e) {
                    logger.warn("can't set {} null", field, e);
                }
            }
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

        boolean reactive = params.getParamContext().handleReactiveArguments(publisher -> {
            if (publisher instanceof Mono) {
                return Mono.from(publisher)
                        .doOnNext(data -> applyUpdateParam(accesses, data));

            }
            if (publisher instanceof Flux) {
                return Flux.from(publisher)
                        .doOnNext(data -> applyUpdateParam(accesses, data));

            }
            return publisher;
        });
        if (reactive) {
            return true;
        }

        applyUpdateParam(accesses, params.getParamContext().getArguments());
        return true;
    }

    @SuppressWarnings("all")
    protected void applyQueryParam(FieldFilterDataAccessConfig config, Object param) {
        if (param instanceof QueryParam) {
            Set<String> denyFields = config.getFields();
            ((QueryParam) param).excludes(denyFields.toArray(new String[0]));
            return;
        }

        Object r = InvokeResultUtils.convertRealResult(param);
        if (r instanceof Collection) {
            ((Collection) r).forEach(o -> setObjectPropertyNull(o, config.getFields()));
        } else {
            setObjectPropertyNull(r, config.getFields());
        }

    }

    @SuppressWarnings("all")
    protected boolean doQueryAccess(FieldFilterDataAccessConfig access, AuthorizingContext context) {
        if (context.getDefinition().getResources().getPhased() == Phased.before) {

            boolean reactive = context
                    .getParamContext()
                    .handleReactiveArguments(publisher -> {
                        if (publisher instanceof Mono) {
                            return Mono.from(publisher)
                                    .doOnNext(param -> {
                                        applyQueryParam(access, param);
                                    });
                        }
                        return publisher;
                    });

            if (reactive) {
                return true;
            }

            for (Object argument : context.getParamContext().getArguments()) {
                applyQueryParam(access, argument);
            }
        } else {
            if (context.getParamContext().getInvokeResult() instanceof Publisher) {
                context.getParamContext().setInvokeResult(
                        Flux.from((Publisher<?>) context.getParamContext().getInvokeResult())
                                .doOnNext(result -> {
                                    applyQueryParam(access, result);
                                })
                );

                return true;
            }
            applyQueryParam(access, context.getParamContext().getInvokeResult());
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
