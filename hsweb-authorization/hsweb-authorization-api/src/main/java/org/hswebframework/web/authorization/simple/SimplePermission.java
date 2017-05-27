package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimplePermission implements Permission {

    private String id;

    private Set<String> actions;

    private Set<DataAccessConfig> dataAccesses;

    public SimplePermission() {
    }

    public SimplePermission(String id, Set<String> actions) {
        this.id = id;
        this.actions = actions;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Set<String> getActions() {
        return actions;
    }

    public void setActions(Set<String> actions) {
        this.actions = actions;
    }

    @Override
    public Set<DataAccessConfig> getDataAccesses() {
        return dataAccesses;
    }

    public void setDataAccesses(Set<DataAccessConfig> dataAccesses) {
        this.dataAccesses = dataAccesses;
    }
}
