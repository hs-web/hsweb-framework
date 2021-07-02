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

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.exception.AccessDenyException;
import org.hswebframework.web.authorization.token.event.UserTokenChangedEvent;
import org.hswebframework.web.authorization.token.event.UserTokenCreatedEvent;
import org.hswebframework.web.authorization.token.event.UserTokenRemovedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 默认到用户令牌管理器，使用ConcurrentMap来存储令牌信息
 *
 * @author zhouhao
 * @since 3.0
 */
public class DefaultUserTokenManager implements UserTokenManager {

    protected final ConcurrentMap<String, LocalUserToken> tokenStorage;

    protected final ConcurrentMap<String, Set<String>> userStorage;


    @Getter
    @Setter
    private Map<String, AllopatricLoginMode> allopatricLoginModes = new HashMap<>();


    public DefaultUserTokenManager() {
        this(new ConcurrentHashMap<>(256));

    }

    public DefaultUserTokenManager(ConcurrentMap<String, LocalUserToken> tokenStorage) {
        this(tokenStorage, new ConcurrentHashMap<>());
    }

    public DefaultUserTokenManager(ConcurrentMap<String, LocalUserToken> tokenStorage, ConcurrentMap<String, Set<String>> userStorage) {
        this.tokenStorage = tokenStorage;
        this.userStorage = userStorage;
    }

    //异地登录模式，默认允许异地登录
    private AllopatricLoginMode allopatricLoginMode = AllopatricLoginMode.allow;

    //事件转发器
    private ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setAllopatricLoginMode(AllopatricLoginMode allopatricLoginMode) {
        this.allopatricLoginMode = allopatricLoginMode;
    }

    public AllopatricLoginMode getAllopatricLoginMode() {
        return allopatricLoginMode;
    }

    protected Set<String> getUserToken(String userId) {
        return userStorage.computeIfAbsent(userId, key -> new HashSet<>());
    }

    private Mono<UserToken> checkTimeout(UserToken detail) {
        if (null == detail) {
            return Mono.empty();
        }
        if (detail.getMaxInactiveInterval() <= 0) {
            return Mono.just(detail);
        }
        if (System.currentTimeMillis() - detail.getLastRequestTime() > detail.getMaxInactiveInterval()) {
            return changeTokenState(detail, TokenState.expired)
                    .thenReturn(detail);
        }
        return Mono.just(detail);
    }

    @Override
    public Mono<UserToken> getByToken(String token) {
        if (token == null) {
            return Mono.empty();
        }
        return checkTimeout(tokenStorage.get(token));
    }

    @Override
    public Flux<UserToken> getByUserId(String userId) {
        if (userId == null) {
            return Flux.empty();
        }
        Set<String> tokens = getUserToken(userId);
        if (tokens.isEmpty()) {
            userStorage.remove(userId);
            return Flux.empty();
        }
        return Flux.fromStream(tokens
                .stream()
                .map(tokenStorage::get)
                .filter(Objects::nonNull));
    }

    @Override
    public Mono<Boolean> userIsLoggedIn(String userId) {
        if (userId == null) {
            return Mono.just(false);
        }
        return getByUserId(userId)
                .any(UserToken::isNormal);
    }

    @Override
    public Mono<Boolean> tokenIsLoggedIn(String token) {
        if (token == null) {
            return Mono.just(false);
        }
        return getByToken(token)
                .map(t -> !t.isExpired())
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<Integer> totalUser() {
        return Mono.just(userStorage.size());
    }

    @Override
    public Mono<Integer> totalToken() {
        return Mono.just(tokenStorage.size());
    }

    @Override
    public Flux<UserToken> allLoggedUser() {
        return Flux.fromIterable(tokenStorage.values());
    }

    @Override
    public Mono<Void> signOutByUserId(String userId) {
        if (null == userId) {
            return Mono.empty();
        }
        return Mono.defer(() -> {
            Set<String> tokens = getUserToken(userId);
            tokens.forEach(token -> signOutByToken(token, false));
            tokens.clear();
            userStorage.remove(userId);
            return Mono.empty();
        });
    }

    private void signOutByToken(String token, boolean removeUserToken) {
        if (token == null) {
            return;
        }
        LocalUserToken tokenObject = tokenStorage.remove(token);
        if (tokenObject != null) {
            String userId = tokenObject.getUserId();
            if (removeUserToken) {
                Set<String> tokens = getUserToken(userId);
                if (!tokens.isEmpty()) {
                    tokens.remove(token);
                }
                if (tokens.isEmpty()) {
                    userStorage.remove(tokenObject.getUserId());
                }
            }
            publishEvent(new UserTokenRemovedEvent(tokenObject));
        }
    }

    @Override
    public Mono<Void> signOutByToken(String token) {
        return Mono.fromRunnable(() -> signOutByToken(token, true));
    }

    protected void publishEvent(ApplicationEvent event) {
        if (null != eventPublisher) {
            eventPublisher.publishEvent(event);
        }
    }

    public Mono<Void> changeTokenState(UserToken userToken, TokenState state) {
        if (null != userToken) {
            LocalUserToken token = ((LocalUserToken) userToken);
            LocalUserToken copy = token.copy();

            token.setState(state);
            syncToken(userToken);

            publishEvent(new UserTokenChangedEvent(copy, userToken));
        }
        return Mono.empty();
    }

    @Override
    public Mono<Void> changeTokenState(String token, TokenState state) {
        return getByToken(token)
                .flatMap(t -> changeTokenState(t, state));
    }

    @Override
    public Mono<Void> changeUserState(String user, TokenState state) {
        return Mono.from(getByUserId(user)
                .flatMap(token -> changeTokenState(token.getToken(), state)));
    }

    @Override
    public Mono<UserToken> signIn(String token, String type, String userId, long maxInactiveInterval) {

        return Mono.defer(() -> {
            LocalUserToken detail = new LocalUserToken(userId, token);
            detail.setType(type);
            detail.setMaxInactiveInterval(maxInactiveInterval);
            detail.setState(TokenState.normal);
            Runnable doSign = () -> {
                tokenStorage.put(token, detail);

                getUserToken(userId).add(token);

                publishEvent(new UserTokenCreatedEvent(detail));
            };
            AllopatricLoginMode mode = allopatricLoginModes.getOrDefault(type, allopatricLoginMode);
            if (mode == AllopatricLoginMode.deny) {
                return getByUserId(userId)
                        .filter(userToken -> type.equals(userToken.getType()))
                        .flatMap(this::checkTimeout)
                        .filterWhen(t -> {
                            if (t.isNormal()) {
                                return Mono.error(new AccessDenyException("error.logged_in_elsewhere"));
                            }
                            return Mono.empty();
                        })
                        .then(Mono.just(detail))
                        .doOnNext(__ -> doSign.run());
            } else if (mode == AllopatricLoginMode.offlineOther) {
                return getByUserId(userId)
                        .filter(userToken -> type.equals(userToken.getType()))
                        .flatMap(userToken -> changeTokenState(userToken, TokenState.offline))
                        .then(Mono.just(detail))
                        .doOnNext(__ -> doSign.run());
            }
            doSign.run();
            return Mono.just(detail);
        });

    }

    @Override
    public Mono<Void> touch(String token) {
        LocalUserToken userToken = tokenStorage.get(token);
        if (null != userToken) {
            userToken.touch();
            syncToken(userToken);
        }
        return Mono.empty();
    }

    @Override
    public Mono<Void> checkExpiredToken() {

        return Flux
                .fromIterable(tokenStorage.values())
                .doOnNext(this::checkTimeout)
                .filter(UserToken::isExpired)
                .map(UserToken::getToken)
                .flatMap(this::signOutByToken)
                .then();
    }

    /**
     * 同步令牌信息,如果使用redisson等来存储token，应该重写此方法并调用{@link this#tokenStorage}.put
     *
     * @param userToken 令牌
     */
    protected void syncToken(UserToken userToken) {
        //do noting
    }
}
