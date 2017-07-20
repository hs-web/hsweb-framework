package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.Role;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleRole implements Role {
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
