package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.dao.authorization.AuthorizationSettingMenuDao;
import org.hswebframework.web.entity.authorization.AuthorizationSettingMenuEntity;
import org.hswebframework.web.entity.authorization.MenuEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.hswebframework.web.service.authorization.AuthorizationSettingMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Service("authorizationSettingMenuService")
public class SimpleAuthorizationSettingMenuService extends AbstractTreeSortService<AuthorizationSettingMenuEntity, String>
        implements AuthorizationSettingMenuService {

    private AuthorizationSettingMenuDao authorizationSettingMenuDao;

    @Override
    public int deleteBySettingId(String settingId) {
        Objects.requireNonNull(settingId);
        return createDelete().where(AuthorizationSettingMenuEntity.settingId, settingId).exec();
    }

    @Override
    public List<AuthorizationSettingMenuEntity> selectBySettingId(String settingId) {
        Objects.requireNonNull(settingId);
        return createQuery().where(AuthorizationSettingMenuEntity.settingId, settingId).listNoPaging();
    }

    @Override
    public List<AuthorizationSettingMenuEntity> selectBySettingId(List<String> settingId) {
        Objects.requireNonNull(settingId);
        return createQuery().where().in(AuthorizationSettingMenuEntity.settingId, settingId).listNoPaging();
    }

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public AuthorizationSettingMenuDao getDao() {
        return authorizationSettingMenuDao;
    }

    @Autowired
    public void setAuthorizationSettingMenuDao(AuthorizationSettingMenuDao authorizationSettingMenuDao) {
        this.authorizationSettingMenuDao = authorizationSettingMenuDao;
    }
}
