package org.hswebframework.web.service.authorization;


import java.util.Objects;
import java.util.Set;

/**
 * 权限设置类型提供者,在初始化权限信息的时候,用于获取被授权用户持有的设置类型.
 * 通过实现此接口,可实现多维度的通用权限设置
 * @author zhouhao
 * @since 3.0
 */
public interface AuthorizationSettingTypeSupplier {

    String SETTING_TYPE_ROLE = "role";
    String SETTING_TYPE_USER = "user";

    /**
     * @param userId 用户ID
     * @return 用户的设置信息
     */
    Set<SettingInfo> get(String userId);

    class SettingInfo {

        /**
         * 设置类型 如: user,role,position,person等等
         *
         * @see org.hswebframework.web.entity.authorization.AuthorizationSettingEntity#type
         */
        private String type;

        /**
         * type对应的主键信息,如 user.id
         *
         * @see org.hswebframework.web.entity.authorization.AuthorizationSettingEntity#settingFor
         */
        private String settingFor;

        @Override
        public int hashCode() {
            return String.valueOf(type).concat(settingFor).hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof SettingInfo && hashCode() == obj.hashCode();
        }

        public SettingInfo(String type, String settingFor) {
            this.type = Objects.requireNonNull(type);
            this.settingFor = Objects.requireNonNull(settingFor);
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSettingFor() {
            return settingFor;
        }

        public void setSettingFor(String settingFor) {
            this.settingFor = settingFor;
        }
    }
}
