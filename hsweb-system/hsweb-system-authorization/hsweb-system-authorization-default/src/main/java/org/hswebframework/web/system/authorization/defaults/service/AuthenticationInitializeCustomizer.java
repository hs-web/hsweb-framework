package org.hswebframework.web.system.authorization.defaults.service;

import org.hswebframework.web.authorization.DimensionType;

/**
 * 认证信息初始化自定义接口
 *
 * @apiNote
 * @since 5.0.1
 */
public interface AuthenticationInitializeCustomizer {

    void customize(Context context);


    interface Context {

        /**
         * 启动某个维度的权限设置功能
         *
         * @param dimensionType 维度类型
         * @see DimensionType#getId()
         * @see org.hswebframework.web.system.authorization.api.entity.AuthorizationSettingEntity
         * @see DefaultAuthorizationSettingService
         */
        void enableDimension(String dimensionType);
    }
}
