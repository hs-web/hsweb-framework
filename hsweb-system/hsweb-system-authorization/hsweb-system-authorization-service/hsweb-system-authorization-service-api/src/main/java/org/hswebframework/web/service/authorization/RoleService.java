package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.service.CrudService;

/**
 * 角色服务
 *
 * @author zhouhao
 * @since 3.0
 */
public interface RoleService extends CrudService<RoleEntity, String> {
    void enable(String roleId);

    void disable(String roleId);
}
