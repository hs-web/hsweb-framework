package org.hswebframework.web.authorization.setting;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public interface UserSettingManager {

    SettingValueHolder getSetting(String userId, String key);

    void saveSetting(String userId, String key, String value, UserSettingPermission permission);

}
