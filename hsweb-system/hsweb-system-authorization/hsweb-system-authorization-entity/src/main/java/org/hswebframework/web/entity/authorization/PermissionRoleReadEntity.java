package org.hswebframework.web.entity.authorization;

import org.hswebframework.web.commons.entity.Entity;

import java.util.List;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface PermissionRoleReadEntity extends Entity {

    String getRoleId();

    String getPermissionId();

    List<String> getActions();
}
