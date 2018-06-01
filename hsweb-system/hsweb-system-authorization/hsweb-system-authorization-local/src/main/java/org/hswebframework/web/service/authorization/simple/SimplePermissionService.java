package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.dao.authorization.PermissionDao;
import org.hswebframework.web.entity.authorization.PermissionEntity;
import org.hswebframework.web.id.IDGenerator;
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
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public PermissionDao getDao() {
        return permissionDao;
    }

    @Override
    public String insert(PermissionEntity entity) {
        entity.setStatus(DataStatus.STATUS_ENABLED);
        return super.insert(entity);
    }

}
