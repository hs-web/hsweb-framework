package org.hswebframework.web.authorization.basic.define;

import lombok.*;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.authorization.annotation.RequiresExpression;
import org.hswebframework.web.authorization.define.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 默认权限权限定义
 *
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DefaultBasicAuthorizeDefinition implements AopAuthorizeDefinition {
    private boolean dataAccessControl;

    private String[] permissionDescription = {};

    private String[] actionDescription = {};

    private Set<String> permissions = new LinkedHashSet<>();

    private Set<String> actions = new LinkedHashSet<>();

    private Set<String> roles = new LinkedHashSet<>();

    private Set<String> user = new LinkedHashSet<>();

    private Script script;

    private String message = "{un_authorized}";

    private Logical logical = Logical.DEFAULT;

    private DataAccessDefinition dataAccessDefinition;

    private Phased phased = Phased.before;

    private Class targetClass;

    private Method targetMethod;

    @Override
    public Phased getPhased() {
        return phased;
    }

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isEmpty() {
        return permissions.isEmpty() && roles.isEmpty() && user.isEmpty() && script == null && dataAccessDefinition == null;
    }

    public void put(Authorize authorize) {
        if (null == authorize || authorize.ignore()) {
            return;
        }
        permissions.addAll(Arrays.asList(authorize.permission()));
        actions.addAll(Arrays.asList(authorize.action()));
        roles.addAll(Arrays.asList(authorize.role()));
        user.addAll(Arrays.asList(authorize.user()));
        if (authorize.logical() != Logical.DEFAULT) {
            logical = authorize.logical();
        }
        message = authorize.message();
        phased = authorize.phased();
        put(authorize.dataAccess());
    }

    public void put(RequiresExpression expression) {
        if (null == expression) {
            return;
        }
        script = new DefaultScript(expression.language(), expression.value());
    }

    public void put(RequiresDataAccess dataAccess) {
        if (null == dataAccess || dataAccess.ignore()) {
            return;
        }
        if (!"".equals(dataAccess.permission())) {
            permissions.add(dataAccess.permission());
        }
        actions.addAll(Arrays.asList(dataAccess.action()));
        DefaultDataAccessDefinition definition = new DefaultDataAccessDefinition();
        definition.setPhased(dataAccess.phased());
        if (!"".equals(dataAccess.controllerBeanName())) {
            definition.setController(dataAccess.controllerBeanName());
        } else if (DataAccessController.class != dataAccess.controllerClass()) {
            definition.setController(dataAccess.getClass().getName());
        }
        dataAccessDefinition = definition;
        dataAccessControl=true;
    }


}
