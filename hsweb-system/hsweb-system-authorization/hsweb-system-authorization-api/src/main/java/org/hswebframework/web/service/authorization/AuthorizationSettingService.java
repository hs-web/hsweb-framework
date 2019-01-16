/*
 *  Copyright 2019 http://www.hswebframework.org
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

import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.entity.authorization.AuthorizationSettingEntity;
import org.hswebframework.web.service.CrudService;

import java.util.List;

/**
 * 权限设置 服务类,提供通用的权限设置. 通过此服务,可实现对用户权限的多维度,自定义,可拓展的权限设置.<br>
 * 例如: 可对用户自身设置权限信息,可对角色设置权限信息,可对机构,部门设置权限信息。各个维度的权限使用{@link AuthorizationSettingTypeSupplier}进行绑定.
 *
 * @author zhouhao
 * @see AuthorizationSettingTypeSupplier
 * @see org.hswebframework.web.authorization.AuthenticationInitializeService
 * @since 3.0
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

    /**
     * 根据类型和被设置者初始化对应的权限信息
     *
     * @param type       设置类型 {@link AuthorizationSettingEntity#getType()}
     * @param settingFor {@link AuthorizationSettingEntity#getSettingFor()}
     * @return 权限信息, 如果没有设置则返回<code>new java.util.ArrayList</code>
     * @see Permission
     * @since 3.0.3
     */
    List<Permission> initPermission(String type, String settingFor);
}
