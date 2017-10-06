package org.hswebframework.web.authorization.basic.define;

import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.annotation.Authorize;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.authorization.annotation.RequiresDataAccess;
import org.hswebframework.web.authorization.annotation.RequiresExpression;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.DataAccessDefinition;
import org.hswebframework.web.authorization.define.Script;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 默认权限权限定义
 *
 * @author zhouhao
 * @since 3.0
 */
public class DefaultBasicAuthorizeDefinition implements AuthorizeDefinition {
    private boolean dataAccessControl;

    private Set<String> permissions = new HashSet<>();

    private Set<String> actions = new HashSet<>();

    private Set<String> roles = new HashSet<>();

    private Set<String> user = new HashSet<>();

    private Script script;

    private String message = "{un_authorized}";

    private Logical logical = Logical.DEFAULT;

    private DataAccessDefinition dataAccessDefinition;

    @Override
    public int getPriority() {
        return Integer.MIN_VALUE;
    }

    @Override
    public boolean isDataAccessControl() {
        return dataAccessControl;
    }

    @Override
    public Set<String> getPermissions() {
        return new HashSet<>(permissions);
    }

    @Override
    public Set<String> getActions() {
        return new HashSet<>(actions);
    }

    @Override
    public Set<String> getRoles() {
        return new HashSet<>(roles);
    }

    @Override
    public Set<String> getUser() {
        return new HashSet<>(user);
    }

    @Override
    public Script getScript() {
        return script;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Logical getLogical() {
        return logical;
    }

    public boolean isEmpty() {
        return permissions.isEmpty() && roles.isEmpty() && user.isEmpty() && script == null && dataAccessDefinition == null;
    }

    @Override
    public DataAccessDefinition getDataAccessDefinition() {
        return dataAccessDefinition;
    }

    public void setDataAccessDefinition(DataAccessDefinition dataAccessDefinition) {
        this.dataAccessDefinition = dataAccessDefinition;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    public void setDataAccessControl(boolean dataAccessControl) {
        this.dataAccessControl = dataAccessControl;
    }

    public void setLogical(Logical logical) {
        this.logical = logical;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public void setScript(Script script) {
        this.script = script;
    }

    public void setUser(Set<String> user) {
        this.user = user;
    }

    public void put(Authorize authorize) {
        if (null == authorize || authorize.ignore()) return;
        permissions.addAll(Arrays.asList(authorize.permission()));
        actions.addAll(Arrays.asList(authorize.action()));
        roles.addAll(Arrays.asList(authorize.role()));
        user.addAll(Arrays.asList(authorize.user()));
        if (authorize.logical() != Logical.DEFAULT) {
            logical = authorize.logical();
        }
        message = authorize.message();
    }

    public void put(RequiresExpression expression) {
        if (null == expression) return;
        script = new DefaultScript(expression.language(), expression.value());
    }

    public void put(RequiresDataAccess dataAccess) {
        if (null == dataAccess) return;
        if (!dataAccess.permission().equals("")) {
            permissions.add(dataAccess.permission());
        }
        actions.addAll(Arrays.asList(dataAccess.action()));
        DefaultDataAccessDefinition definition = new DefaultDataAccessDefinition();

        if (!"".equals(dataAccess.controllerBeanName())) {
            definition.setController(dataAccess.controllerBeanName());
        } else if (DataAccessController.class != dataAccess.controllerClass()) {
            definition.setController(dataAccess.getClass().getName());
        }
        dataAccessDefinition = definition;
    }


}
