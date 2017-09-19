package org.hswebframework.web.authorization.basic.handler;

import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.expands.script.engine.DynamicScriptEngine;
import org.hswebframework.expands.script.engine.DynamicScriptEngineFactory;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.access.DataAccessConfig;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.AuthorizingContext;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.boost.aop.context.MethodInterceptorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
public class DefaultAuthorizingHandler implements AuthorizingHandler {

    private DataAccessController dataAccessController;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public DefaultAuthorizingHandler(DataAccessController dataAccessController) {
        this.dataAccessController = dataAccessController;
    }

    public DefaultAuthorizingHandler() {
    }

    public void setDataAccessController(DataAccessController dataAccessController) {
        this.dataAccessController = dataAccessController;
    }

    @Override
    public void handle(AuthorizingContext context) {

        //进行rdac权限控制
        handleRdac(context.getAuthentication(), context.getDefinition());

        //进行数据权限控制
        handleDataAccess(context);

        //表达式权限控制
        handleExpression(context.getAuthentication(), context.getDefinition(), context.getParamContext());

    }

    protected void handleDataAccess(AuthorizingContext context) {
        if (dataAccessController == null) {
            logger.warn("dataAccessController is null,skip result access control!");
            return;
        }
        List<Permission> permission = context.getAuthentication().getPermissions()
                .stream()
                .filter(per -> context.getDefinition().getPermissions().contains(per.getId()))
                .collect(Collectors.toList());

        DataAccessController finalAccessController = dataAccessController;

        //取得当前登录用户持有的控制规则
        Set<DataAccessConfig> accesses = permission
                .stream().map(Permission::getDataAccesses)
                .flatMap(Collection::stream)
                .filter(access -> context.getDefinition().getActions().contains(access.getAction()))
                .collect(Collectors.toSet());
        //无规则,则代表不进行控制
        if (accesses.isEmpty()) return;
        //单个规则验证函数
        Function<Predicate<DataAccessConfig>, Boolean> function = accesses.stream()::allMatch;
        //调用控制器进行验证
        boolean isAccess = function.apply(access -> finalAccessController.doAccess(access, context));
        if (!isAccess) {
            throw new AccessDenyException(context.getDefinition().getMessage());
        }

    }

    protected void handleExpression(Authentication authentication, AuthorizeDefinition definition, MethodInterceptorContext paramContext) {
        if (definition.getScript() != null) {
            String scriptId = DigestUtils.md5Hex(definition.getScript().getScript());

            DynamicScriptEngine engine = DynamicScriptEngineFactory.getEngine(definition.getScript().getLanguage());
            if (null == engine) {
                throw new AccessDenyException("{unknown_engine}:" + definition.getScript().getLanguage());
            }
            if (!engine.compiled(scriptId)) {
                try {
                    engine.compile(scriptId, definition.getScript().getScript());
                } catch (Exception e) {
                    logger.error("express compile error", e);
                    throw new AccessDenyException("{expression_error}");
                }
            }
            Map<String, Object> var = new HashMap<>(paramContext.getParams());
            var.put("auth", authentication);
            Object success = engine.execute(scriptId, var).get();
            if (!(success instanceof Boolean) || !((Boolean) success)) {
                throw new AccessDenyException(definition.getMessage());
            }
        }
    }

    protected void handleRdac(Authentication authentication, AuthorizeDefinition definition) {
        boolean access = true;
        //多个设置时的判断逻辑
        Logical logical = definition.getLogical() == Logical.DEFAULT ? Logical.OR : definition.getLogical();
        boolean logicalIsOr = logical == Logical.OR;

        Set<String> permissionsDef = definition.getPermissions();
        Set<String> actionsDef = definition.getActions();
        Set<String> rolesDef = definition.getRoles();
        Set<String> usersDef = definition.getUser();


        // 控制权限
        if (!definition.getPermissions().isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("do permission access handle : permissions{},actions{} ", permissionsDef, actionsDef);
            }
            List<Permission> permissions = authentication.getPermissions().stream()
                    .filter(permission -> {
                        // 未持有任何一个权限
                        if (!permissionsDef.contains(permission.getId())) return false;
                        //未配置action
                        if (actionsDef.isEmpty())
                            return true;
                        //判断action
                        List<String> actions = permission.getActions()
                                .stream()
                                .filter(actionsDef::contains)
                                .collect(Collectors.toList());

                        if (actions.isEmpty()) return false;

                        //如果 控制逻辑是or,则只要过滤结果数量不为0.否则过滤结果数量必须和配置的数量相同
                        return logicalIsOr ? actions.size() > 0 : permission.getActions().containsAll(actions);
                    }).collect(Collectors.toList());
            access = logicalIsOr ?
                    permissions.size() > 0 :
                    //权限数量和配置的数量相同
                    permissions.size() == permissionsDef.size();
        }
        //控制角色
        if (!rolesDef.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("do role access handle : roles{} ", rolesDef);
            }
            Function<Predicate<Role>, Boolean> func = logicalIsOr
                    ? authentication.getRoles().stream()::anyMatch
                    : authentication.getRoles().stream()::allMatch;
            access = func.apply(role -> rolesDef.contains(role.getId()));
        }
        //控制用户
        if (!usersDef.isEmpty()) {
            if (logger.isInfoEnabled()) {
                logger.info("do user access handle : users{} ", usersDef);
            }
            Function<Predicate<String>, Boolean> func = logicalIsOr
                    ? usersDef.stream()::anyMatch
                    : usersDef.stream()::allMatch;
            access = func.apply(authentication.getUser().getUsername()::equals);
        }
        if (!access) {
            throw new AccessDenyException(definition.getMessage());
        }
    }
}
