package org.hswebframework.web.authorization.twofactor;

/**
 * @author zhouhao
 * @since 3.0.4
 */
public interface TwoFactorValidatorProvider {

    String getProvider();

    TwoFactorValidator createTwoFactorValidator(String userId,String operation);
}
