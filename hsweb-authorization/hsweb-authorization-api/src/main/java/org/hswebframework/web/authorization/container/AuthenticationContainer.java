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

import java.util.List;

/**
 * 授权容器,用来操作所有已经授权的用户
 *
 * @author zhouhao
 * @since 3.0
 */
public interface AuthenticationContainer {

    /**
     * 根据token获取权限信息
     *
     * @param token
     * @return 权限信息, 未授权时返回null
     */
    UserToken getByToken(String token);

    /**
     * 根据用户id，获取全部授权信息，如果设置了不能跨地点登陆，返回值只可能是{@code null}或者size为1的list
     * @param userId 用户id
     * @return 授权信息
     */
    List<UserToken> getByUserId(String userId);

    /**
     * @param userId 用户ID
     * @return 用户是否已经授权
     */
    boolean userIsLoggedIn(String userId);

    boolean tokenIsLoggedIn(String token);

    /**
     * @return 总用户数量，一个用户多个地方登陆数量算1
     */
    int totalUser();

    /**
     *
     * @return 总token数量
     */
    int totalToken();
    /**
     * @return 所有被授权的用户
     */
    List<UserToken> allLoggedUser();

    /**
     * 删除用户授权信息
     *
     * @param userId 用户ID
     */
    void logoutByUserId(String userId);

    /**
     * 根据token删除
     * @param token
     */
    void logoutByToken(String token);

    /**
     * @param token
     * @param userId
     */
    UserToken signIn(String token, String userId);


    void touch(String token);
}
