package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.Date;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleUserEntity extends SimpleGenericEntity<String> implements UserEntity {
    private String name;

    private String username;

    private String password;

    private String salt;

    private Date createDate;

    private Date lastLoginDate;

    private boolean enabled;

    private String lastLoginIp;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(Date lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    @Override
    public SimpleUserEntity clone() {
        SimpleUserEntity target = new SimpleUserEntity();
        target.setId(getId());
        target.setName(getName());
        target.setUsername(getUsername());
        target.setPassword(getPassword());
        target.setCreateDate(getCreateDate());
        target.setEnabled(isEnabled());
        target.setLastLoginDate(getLastLoginDate());
        target.setLastLoginIp(getLastLoginIp());
        return target;
    }
}
