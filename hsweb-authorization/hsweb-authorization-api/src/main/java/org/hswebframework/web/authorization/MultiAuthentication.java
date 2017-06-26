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

import java.util.Set;

/**
 * 多用户权限,可同时登录多个用户,调用{@link Authentication}的方法为获取当前激活用户的权限
 *
 * @since 3.0
 */
public interface MultiAuthentication extends Authentication {

    /**
     * @return 所有权限信息
     */
    Set<Authentication> getAuthentications();

    /**
     * 激活指定的用户
     *
     * @param userId 用户ID
     * @return 被激活的用户, 如果用户未登录, 则返回null
     */
    Authentication activate(String userId);

    /**
     * 添加一个授权
     *
     * @param authentication 授权信息
     */
    void addAuthentication(Authentication authentication);
}
