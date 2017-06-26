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

package org.hswebframework.web.authorization.container;

import org.hswebframework.web.authorization.Authentication;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * 授权容器,用来操作所有已经授权的用户
 *
 * @author zhouhao
 * @since 3.0
 */
public interface AuthenticationContainer {

    /**
     * 根据sessionId获取权限信息
     *
     * @param sessionId
     * @return 权限信息, 未授权时返回null
     */
    Authentication getAuthenticationBySessionId(String sessionId);

    /**
     * @param userId 用户ID
     * @return 用户是否已经授权
     */
    boolean userIsAuthorized(String userId);

    /**
     * @return 已经授权的总人数
     */
    int totalAuthorizedUser();

    /**
     * @return 所有被授权的用户
     */
    List<Authentication> allAuthorizedUser();

    /**
     * 删除用户授权信息
     *
     * @param userId 用户ID
     * @return 被删除的权限信息
     */
    Authentication removeAuthentication(String userId);

    /**
     * @param authentication
     * @return 添加后被覆盖的权限信息 ,如果没有则返回null
     */
    Authentication addAuthentication(Authentication authentication, String sessionId);
}
