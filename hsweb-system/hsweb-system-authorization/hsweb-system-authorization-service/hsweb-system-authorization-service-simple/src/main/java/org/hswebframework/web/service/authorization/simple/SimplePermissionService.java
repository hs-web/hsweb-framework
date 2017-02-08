package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.dao.authorization.PermissionDao;
import org.hswebframework.web.entity.authorization.PermissionEntity;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.authorization.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Service("permissionService")
public class SimplePermissionService extends GenericEntityService<PermissionEntity, String>
        implements PermissionService {
    @Autowired
    private PermissionDao permissionDao;

    @Override
    public PermissionDao getDao() {
        return permissionDao;
    }

}
