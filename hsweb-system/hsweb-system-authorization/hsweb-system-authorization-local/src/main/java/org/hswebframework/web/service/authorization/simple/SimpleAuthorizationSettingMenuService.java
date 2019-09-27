package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.entity.authorization.AuthorizationSettingMenuEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.AbstractTreeSortService;
import org.hswebframework.web.service.authorization.AuthorizationSettingMenuService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author zhouhao
 */
@Service("authorizationSettingMenuService")
public class SimpleAuthorizationSettingMenuService extends AbstractTreeSortService<AuthorizationSettingMenuEntity, String>
        implements AuthorizationSettingMenuService {

    @Override
    public int deleteBySettingId(String settingId) {
        Objects.requireNonNull(settingId);
        return getDao().createDelete()
                .where(AuthorizationSettingMenuEntity.settingId, settingId)
                .execute();
    }

    @Override
    public List<AuthorizationSettingMenuEntity> selectBySettingId(String settingId) {
        Objects.requireNonNull(settingId);
        return createQuery().where(AuthorizationSettingMenuEntity.settingId, settingId).fetch();
    }

    @Override
    public List<AuthorizationSettingMenuEntity> selectBySettingId(List<String> settingId) {
        if(CollectionUtils.isEmpty(settingId)){
            return new ArrayList<>();
        }
        return createQuery().where().in(AuthorizationSettingMenuEntity.settingId, settingId).fetch();
    }

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }


}
