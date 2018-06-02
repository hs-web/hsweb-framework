package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.RoleEntity;
import org.hswebframework.web.service.CrudService;

/**
 * 角色服务,就是一个简单的crud
 *
 * @author zhouhao
 * @since 3.0
 */
public interface RoleService extends CrudService<RoleEntity, String> {
    /**
     * 启用角色
     *
     * @param roleId 角色ID
     * @see RoleEntity#setStatus(Byte)
     * @see org.hswebframework.web.commons.entity.DataStatus#STATUS_ENABLED
     */
    void enable(String roleId);

    /**
     * 禁用角色
     *
     * @param roleId 角色ID
     * @see RoleEntity#setStatus(Byte)
     * @see org.hswebframework.web.commons.entity.DataStatus#STATUS_DISABLED
     */
    void disable(String roleId);
}
