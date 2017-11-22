package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.Role;

/**
 * @author zhouhao
 */
public class SimpleRole implements Role {

    private static final long serialVersionUID = 7460859165231311347L;

    private String id;

    private String name;

    public SimpleRole() {
    }

    public SimpleRole(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
