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
 *
 */

package org.hswebframework.web.authorization;

import reactor.core.publisher.Mono;

/**
 * 授权信息管理器,用于获取用户授权和同步授权信息
 *
 * @author zhouhao
 * @see 3.0
 */
public interface ReactiveAuthenticationManager {

    /**
     * 进行授权操作
     *
     * @param request 授权请求
     * @return 授权成功则返回用户权限信息
     */
    Mono<Authentication> authenticate(Mono<AuthenticationRequest> request);

    /**
     * 根据用户ID获取权限信息
     *
     * @param userId 用户ID
     * @return 权限信息
     */
    Mono<Authentication> getByUserId(String userId);


}
