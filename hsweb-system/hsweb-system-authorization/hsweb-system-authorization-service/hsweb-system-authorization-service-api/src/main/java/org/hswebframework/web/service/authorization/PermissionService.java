package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.ActionEntity;
import org.hswebframework.web.entity.authorization.PermissionEntity;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.service.CrudService;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface PermissionService<Q extends Entity> extends CrudService<PermissionEntity<ActionEntity>, String, Q> {

}
