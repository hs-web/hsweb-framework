/*
 *  Copyright 2020 http://www.hswebframework.org
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

package org.hswebframework.web.authorization.token;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 用户令牌管理器,用于管理用户令牌
 *
 * @author zhouhao
 * @since 3.0
 */
public interface UserTokenManager {

    /**
     * 根据token获取用户令牌信息
     *
     * @param token token
     * @return 令牌信息, 未授权时返回null
     */
    Mono<UserToken> getByToken(String token);

    /**
     * 根据用户id，获取全部令牌信息，如果没有则返回空集合而不是<code>null</code>
     *
     * @param userId 用户id
     * @return 授权信息
     */
    Flux<UserToken> getByUserId(String userId);

    /**
     * @param userId 用户ID
     * @return 用户是否已经授权
     */
    Mono<Boolean> userIsLoggedIn(String userId);

    /**
     * @param token token
     * @return token是否已登记
     */
    Mono<Boolean> tokenIsLoggedIn(String token);

    /**
     * @return 总用户数量，一个用户多个地方登陆数量算1
     */
    Mono<Integer> totalUser();

    /**
     * @return 总token数量
     */
    Mono<Integer> totalToken();

    /**
     * @return 所有token
     */
    Flux<UserToken> allLoggedUser();

    /**
     * 删除用户授权信息
     *
     * @param userId 用户ID
     */
    Mono<Void> signOutByUserId(String userId);

    /**
     * 根据token删除
     *
     * @param token 令牌
     * @see org.hswebframework.web.authorization.token.event.UserTokenRemovedEvent
     */
    Mono<Void> signOutByToken(String token);

    /**
     * 修改userId的状态
     *
     * @param userId userId
     * @param state  状态
     * @see org.hswebframework.web.authorization.token.event.UserTokenChangedEvent
     * @see UserTokenManager#changeTokenState
     */
    Mono<Void> changeUserState(String userId, TokenState state);

    /**
     * 修改token的状态
     *
     * @param token token
     * @param state 状态
     * @see org.hswebframework.web.authorization.token.event.UserTokenChangedEvent
     */
    Mono<Void> changeTokenState(String token, TokenState state);

    /**
     * 登记一个用户的token
     *
     * @param token               token
     * @param type                令牌类型
     * @param userId              用户id
     * @param maxInactiveInterval 最大不活动时间,超过后令牌状态{@link UserToken#getState()}将变为过期{@link TokenState#expired}
     * @see org.hswebframework.web.authorization.token.event.UserTokenCreatedEvent
     */
    Mono<UserToken> signIn(String token, String type, String userId, long maxInactiveInterval);

    /**
     * 更新token,使其不过期
     *
     * @param token token
     */
    Mono<Void> touch(String token);

    /**
     * 检查已过期的token,并将其remove
     *
     * @see this#signOutByToken(String)
     */
    Mono<Void> checkExpiredToken();

}
