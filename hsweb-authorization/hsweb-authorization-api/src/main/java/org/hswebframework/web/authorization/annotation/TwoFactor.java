package org.hswebframework.web.authorization.annotation;

import org.hswebframework.web.authorization.twofactor.TwoFactorValidator;

import java.lang.annotation.*;

/**
 * 开启2FA双重验证
 *
 * @see org.hswebframework.web.authorization.twofactor.TwoFactorValidatorManager
 * @see org.hswebframework.web.authorization.twofactor.TwoFactorValidatorProvider
 * @see org.hswebframework.web.authorization.twofactor.TwoFactorValidator
 * @since 3.0.4
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface TwoFactor {

    /**
     * @return 接口的标识, 用于实现不同的操作, 可能会配置不同的验证规则
     */
    String value();

    /**
     * @return 验证有效期, 超过有效期后需要重新进行验证
     */
    long timeout() default 10 * 60 * 1000L;

    /**
     * 验证器供应商,如: totp,sms,email,由{@link org.hswebframework.web.authorization.twofactor.TwoFactorValidatorProvider}进行自定义.
     * <p>
     * 可通过配置项: hsweb.authorize.two-factor.default-provider 来修改默认配置
     *
     * @return provider
     * @see TwoFactorValidator#getProvider()
     */
    String provider() default "default";

    /**
     * 验证码的http参数名,在进行验证的时候需要传入此参数
     *
     * @return 验证码的参数名
     */
    String parameter() default "verifyCode";

    /**
     * @return 关闭验证
     */
    boolean ignore() default false;
}
