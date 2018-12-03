package org.hswebframework.web.authorization.twofactor;

import org.hswebframework.web.authorization.Authentication;

/**
 * 双重验证管理器
 */
public interface TwoFactorValidatorManager {

    /**
     * 获取用户使用的双重验证器
     *
     * @param userId    用户id
     * @param operation 进行的操作
     * @return 验证器
     */
    TwoFactorValidator getValidator(String userId, String operation);

}
