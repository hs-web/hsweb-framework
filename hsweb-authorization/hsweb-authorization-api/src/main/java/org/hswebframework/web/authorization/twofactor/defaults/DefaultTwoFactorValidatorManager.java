package org.hswebframework.web.authorization.twofactor.defaults;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidator;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidatorManager;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidatorProvider;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public class DefaultTwoFactorValidatorManager implements TwoFactorValidatorManager {

    @Getter
    @Setter
    private String defaultProvider = "totp";

    private Map<String, TwoFactorValidatorProvider> providers = new HashMap<>();

    @Override
    public TwoFactorValidator getValidator(String userId, String operation, String provider) {
        if (provider == null) {
            provider = defaultProvider;
        }
        TwoFactorValidatorProvider validatorProvider = providers.get(provider);
        if (validatorProvider == null) {
            return new UnsupportedTwoFactorValidator(provider);
        }
        return validatorProvider.createTwoFactorValidator(userId, operation);
    }

}
