package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.UserSettingEntity;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface UserSettingService extends CrudService<UserSettingEntity, String> {
    List<UserSettingEntity> selectByUser(String userId, String key);

    UserSettingEntity selectByUser(String userId, String key, String settingId);
}
