package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.PermissionEntity;
import org.hswebframework.web.service.CrudService;

/**
 * 权限管理服务,就一个简单的crud
 *
 * @author zhouhao
 * @since 3.0
 */
public interface PermissionService extends CrudService<PermissionEntity, String> {

}
