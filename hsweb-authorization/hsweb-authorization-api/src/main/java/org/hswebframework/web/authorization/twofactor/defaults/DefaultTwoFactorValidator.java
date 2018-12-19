package org.hswebframework.web.authorization.twofactor.defaults;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.authorization.twofactor.TwoFactorToken;
import org.hswebframework.web.authorization.twofactor.TwoFactorValidator;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author zhouhao
 * @since 3.0.4
 */
@AllArgsConstructor
public class DefaultTwoFactorValidator implements TwoFactorValidator {

    @Getter
    private String provider;

    private Function<String, Boolean> validator;

    private Supplier<TwoFactorToken> tokenSupplier;

    @Override
    public boolean verify(String code, long timeout) {
        boolean success = validator.apply(code);
        if (success) {
            tokenSupplier.get().generate(timeout);
        }
        return success;
    }

    @Override
    public boolean expired() {
        return tokenSupplier.get().expired();
    }
}
