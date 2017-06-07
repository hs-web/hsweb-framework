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
 *
 */

package org.hswebframework.web.authorization;

/**
 * 授权信息初始化服务接口,使用该接口初始化用的权限信息
 *
 * @author zhouhao
 * @since 3.0
 */
public interface AuthenticationInitializeService {
    /**
     * 根据用户ID初始化权限信息
     *
     * @param userId 用户ID
     * @return 权限信息
     */
    Authentication initUserAuthorization(String userId);

}
