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

import java.io.Serializable;
import java.util.Map;

/**
 * 授权信息管理器,用于获取用户授权和同步授权信息
 *
 * @author zhouhao
 * @see 3.0
 */
public interface AuthenticationManager {
    String USER_AUTH_CACHE_NAME = "user-auth-";

    /**
     * 进行授权操作
     *
     * @param request 授权请求
     * @return 授权成功则返回用户权限信息
     */
    Authentication authenticate(AuthenticationRequest request);

    /**
     * 根据用户ID获取权限信息
     *
     * @param userId 用户ID
     * @return 权限信息
     */
    Authentication getByUserId(String userId);

    /**
     * 同步授权信息,在调用了{@link Authentication#setAttribute(String, Serializable)}或者
     * {@link Authentication#setAttributes(Map)} 后,需要调用次方法进行同步.
     * 因为如果权限信息不是存在于内存中,而是redis或者其他方案.
     * 在调用了上述方法后,实际的存储值并不会发生改变.
     * 注意: Authentication的实现类应该实现自动同步功能。
     *
     * @param authentication 要同步的权限信息
     * @return 同步后的权限信息
     */
    Authentication sync(Authentication authentication);
}
