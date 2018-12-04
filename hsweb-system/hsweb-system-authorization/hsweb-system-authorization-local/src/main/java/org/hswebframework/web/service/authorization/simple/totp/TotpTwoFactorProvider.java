package org.hswebframework.web.service.authorization.simple.totp;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.setting.UserSettingManager;
import org.hswebframework.web.authorization.setting.UserSettingPermission;
import org.hswebframework.web.authorization.twofactor.defaults.DefaultTwoFactorValidatorProvider;
import org.hswebframework.web.authorization.twofactor.TwoFactorTokenManager;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.authorization.events.TotpTwoFactorCreatedEvent;
import org.hswebframework.web.service.authorization.events.UserCreatedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Function;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@Transactional(rollbackFor = Exception.class)
public class TotpTwoFactorProvider extends DefaultTwoFactorValidatorProvider {

    private UserSettingManager userSettingManager;

    @Getter
    @Setter
    private String domain = "hsweb.me";

    @Getter
    @Setter
    private String settingId = "tow-factor-totp-key";

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public TotpTwoFactorProvider(UserSettingManager userSettingManager, TwoFactorTokenManager twoFactorTokenManager) {
        super("totp", twoFactorTokenManager);
        this.userSettingManager = userSettingManager;
    }

    @EventListener
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        //生成totp
        String key = TotpUtil.getRandomSecretBase32(64);
        UserEntity userEntity = event.getUserEntity();
        String keyUrl = TotpUtil.generateTotpString(userEntity.getUsername(), domain, key);
        //创建一个用户没有操作权限的配置
        userSettingManager.saveSetting(userEntity.getId(), settingId, key, UserSettingPermission.NONE);
        eventPublisher.publishEvent(new TotpTwoFactorCreatedEvent(userEntity, keyUrl));
    }

    @Override
    protected boolean validate(String userId, String code) {
        return userSettingManager.getSetting(userId, settingId)
                .asString()
                .map(key -> TotpUtil.verify(key, code))
                .orElse(false);
    }

}
