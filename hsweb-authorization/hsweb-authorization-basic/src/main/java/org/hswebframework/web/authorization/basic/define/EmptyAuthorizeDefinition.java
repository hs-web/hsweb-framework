package org.hswebframework.web.authorization.basic.define;

import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.DataAccessDefinition;
import org.hswebframework.web.authorization.define.Phased;
import org.hswebframework.web.authorization.define.Script;

import java.util.Set;

/**
 * @author zhouhao
 */
public class EmptyAuthorizeDefinition implements AuthorizeDefinition {

    public static final EmptyAuthorizeDefinition instance = new EmptyAuthorizeDefinition();

    private EmptyAuthorizeDefinition() {
    }

    @Override
    public Phased getPhased() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getPriority() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDataAccessControl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getPermissions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getPermissionDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getActionDescription() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getActions() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getRoles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Script getScript() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getMessage() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Logical getLogical() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public DataAccessDefinition getDataAccessDefinition() {
        throw new UnsupportedOperationException();
    }
}
