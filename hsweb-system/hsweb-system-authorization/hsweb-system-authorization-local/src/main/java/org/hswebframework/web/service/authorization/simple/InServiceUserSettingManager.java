package org.hswebframework.web.service.authorization.simple;

import org.hswebframework.web.authorization.setting.SettingValueHolder;
import org.hswebframework.web.authorization.setting.StringSourceSettingHolder;
import org.hswebframework.web.authorization.setting.UserSettingManager;
import org.hswebframework.web.authorization.setting.UserSettingPermission;
import org.hswebframework.web.entity.authorization.UserSettingEntity;
import org.hswebframework.web.service.authorization.UserSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@Service
public class InServiceUserSettingManager implements UserSettingManager {

    @Autowired
    private UserSettingService userSettingService;

    @Override
    public SettingValueHolder getSetting(String userId, String key) {
        UserSettingEntity entity = userSettingService.selectByUser(userId, "user-setting", key);
        if (entity == null) {
            return SettingValueHolder.NULL;
        }
        return StringSourceSettingHolder.of(entity.getSetting(), entity.getPermission());
    }

    @Override
    public void saveSetting(String userId, String key, String value, UserSettingPermission permission) {
        UserSettingEntity entity = new UserSettingEntity();
        entity.setUserId(userId);
        entity.setKey("user-setting");
        entity.setSettingId(key);
        entity.setSetting(value);
        entity.setPermission(permission);
        userSettingService.saveOrUpdate(entity);
    }
}
