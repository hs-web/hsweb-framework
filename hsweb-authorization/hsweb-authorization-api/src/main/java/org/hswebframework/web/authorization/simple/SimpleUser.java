package org.hswebframework.web.authorization.simple;

import org.hswebframework.web.authorization.User;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleUser implements User {
    private String id;
    private String username;
    private String name;

    public SimpleUser() {
    }

    public SimpleUser(String id, String username, String name) {
        this.id = id;
        this.username = username;
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
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
