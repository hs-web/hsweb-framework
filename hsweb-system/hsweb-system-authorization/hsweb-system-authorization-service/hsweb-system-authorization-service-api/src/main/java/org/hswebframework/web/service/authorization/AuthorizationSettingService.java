/*
 *  Copyright 2016 http://www.hswebframework.org
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package org.hswebframework.web.service.authorization;

import org.hswebframework.web.entity.authorization.AuthorizationSettingEntity;
import org.hswebframework.web.service.CrudService;

/**
 * 权限设置 服务类,提供通用的权限设置
 *
 * @author hsweb-generator-online
 */
public interface AuthorizationSettingService extends CrudService<AuthorizationSettingEntity, String> {
    /**
     * 根据类型和被设置者获取配置
     *
     * @param type       设置类型 {@link AuthorizationSettingEntity#getType()}
     * @param settingFor {@link AuthorizationSettingEntity#getSettingFor()}
     * @return 设置内容, 不存在时返回 <code>null</code>
     */
    AuthorizationSettingEntity select(String type, String settingFor);
}
