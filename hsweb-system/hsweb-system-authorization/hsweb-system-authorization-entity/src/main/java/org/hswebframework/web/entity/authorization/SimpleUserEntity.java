package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

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

    private Long createTime;

    private String creatorId;

    private Long lastLoginTime;

    private Boolean enabled;

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

    @Override
    public Long getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String getCreatorId() {
        return creatorId;
    }

    @Override
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public void setLastLoginTime(Long lastLoginDate) {
        this.lastLoginTime = lastLoginDate;
    }

    @Override
    public Long getLastLoginTime() {
        return lastLoginTime;
    }

    @Override
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public Boolean isEnabled() {
        return enabled;
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }

    @Override
    public SimpleUserEntity clone() {
        SimpleUserEntity target = ((SimpleUserEntity) super.clone());
//        target.setId(getId());
//        target.setName(getName());
//        target.setUsername(getUsername());
//        target.setPassword(getPassword());
//        target.setCreateTime(getCreateTime());
//        target.setCreatorId(getCreatorId());
//        target.setEnabled(isEnabled());
//        target.setLastLoginTime(getLastLoginTime());
//        target.setLastLoginIp(getLastLoginIp());
        return target;
    }
}
