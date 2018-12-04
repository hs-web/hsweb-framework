package org.hswebframework.web.authorization.full.controller;

import org.hswebframework.web.authorization.twofactor.TwoFactorValidator;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidatorProvider;
import org.springframework.stereotype.Component;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@Component
public class TestTwoFactorValidatorProvider implements TwoFactorValidatorProvider {
    @Override
    public String getProvider() {
        return "test";
    }

    @Override
    public TwoFactorValidator createTwoFactorValidator(String userId, String operation) {
        return new TwoFactorValidator() {
            boolean success = false;

            @Override
            public String getProvider() {
                return "test";
            }

            @Override
            public boolean verify(String code, long timeout) {
                return success = code.equalsIgnoreCase("test");
            }

            @Override
            public boolean expired() {
                return !success;
            }
        };
    }
}
