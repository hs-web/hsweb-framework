package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.commons.entity.DataStatus;
import org.hswebframework.web.dao.authorization.PermissionDao;
import org.hswebframework.web.entity.authorization.PermissionEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.authorization.PermissionService;
import org.hswebframework.web.service.authorization.events.ClearUserAuthorizationCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 权限管理
 *
 * @author zhouhao
 */
@Service("permissionService")
public class SimplePermissionService extends GenericEntityService<PermissionEntity, String>
        implements PermissionService {
    @Autowired
    private PermissionDao permissionDao;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

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

    @Override
    public int updateByPk(String id, PermissionEntity entity) {
        int len = super.updateByPk(id, entity);
        eventPublisher.publishEvent(new ClearUserAuthorizationCacheEvent(null, true));
        return len;
    }

    @Override
    public PermissionEntity deleteByPk(String id) {
        PermissionEntity old = super.deleteByPk(id);
        eventPublisher.publishEvent(new ClearUserAuthorizationCacheEvent(null, true));
        return old;
    }
}
