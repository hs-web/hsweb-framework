package org.hswebframework.web.entity.organizational;

import org.hswebframework.web.commons.entity.Entity;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class PersonUserEntity implements Entity {
    private String username;

    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
