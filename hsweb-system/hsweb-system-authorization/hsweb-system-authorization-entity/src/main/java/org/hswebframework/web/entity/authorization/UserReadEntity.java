package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.Entity;

import java.util.Date;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface UserReadEntity extends Entity {

    String getId();

    String getName();

    String getUsername();

    Date getCreateDate();

    Date getLastLoginDate();

    boolean isEnabled();

    String getLastLoginIp();
}
