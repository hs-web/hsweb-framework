package org.hswebframework.web.entity.organizational;

import org.hswebframework.web.commons.entity.Entity;

/**
 * @author zhouhao
 */
public class PersonUserEntity implements Entity {
    private static final long serialVersionUID = -2619415787107625818L;
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
