package org.hswebframework.web.authorization.twofactor.defaults;

import lombok.Getter;
import org.hswebframework.web.authorization.twofactor.TwoFactorTokenManager;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidator;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidatorProvider;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@Getter
public abstract class DefaultTwoFactorValidatorProvider implements TwoFactorValidatorProvider {

    private String provider;

    private TwoFactorTokenManager twoFactorTokenManager;

    public DefaultTwoFactorValidatorProvider(String provider, TwoFactorTokenManager twoFactorTokenManager) {
        this.provider = provider;
        this.twoFactorTokenManager = twoFactorTokenManager;
    }

    protected abstract boolean validate(String userId, String code);

    @Override
    public TwoFactorValidator createTwoFactorValidator(String userId, String operation) {
        return new DefaultTwoFactorValidator(getProvider(), code -> validate(userId, code), () -> twoFactorTokenManager.getToken(userId, operation));
    }
}
