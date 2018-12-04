package org.hswebframework.web.authorization.twofactor;

/**
 * 双重验证管理器
 * @author zhouhao
 * @since 3.0.4
 */
public interface TwoFactorValidatorManager {

    /**
     * 获取用户使用的双重验证器
     *
     * @param provider 验证器供应商
     * @return 验证器
     */
    TwoFactorValidator getValidator(String userId,String operation, String provider);

}
