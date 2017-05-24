package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.access.FieldAccessConfig;

import java.util.HashSet;
import java.util.Set;

public class SimpleFieldAccess implements FieldAccessConfig {
    private String      field;
    private Set<String> actions;

    public SimpleFieldAccess() {
    }

    public SimpleFieldAccess(String field, Set<String> actions) {
        this.field = field;
        this.actions = actions;
    }

    @Override
    public String getField() {
        return field;
    }

    @Override
    public Set<String> getActions() {
        return new HashSet<>(actions);
    }

    public void setField(String field) {
        this.field = field;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }
}
