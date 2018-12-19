package org.hswebframework.web.authorization.twofactor;

/**
 * 双重验证器,用于某些接口需要双重验证时使用,如: 短信验证码,动态口令等
 *
 * @author zhouhao
 * @since 3.0.4
 */
public interface TwoFactorValidator {

    String getProvider();

    /**
     * 验证code是否有效,如果验证码有效,则保持此验证有效期.在有效期内,调用{@link this#expired()} 将返回false
     *
     * @param code    验证码
     * @param timeout 保持验证通过有效期
     * @return 验证码是否有效
     */
    boolean verify(String code, long timeout);

    /**
     * 验证是否已经过期,过期则需要重新进行验证
     *
     * @return 是否过期
     */
    boolean expired();
}
