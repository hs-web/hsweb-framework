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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 默认到用户令牌管理器，使用ConcurrentMap来存储令牌信息
 *
 * @author zhouhao
 * @since 3.0
 */
public class DefaultUserTokenManager implements UserTokenManager {

    protected final ConcurrentMap<String, SimpleUserToken> tokenStorage;

    protected final ConcurrentMap<String, Set<String>> userStorage;


    @Getter
    @Setter
    private Map<String, AllopatricLoginMode> allopatricLoginModes = new HashMap<>();


    public DefaultUserTokenManager() {
        this(new ConcurrentHashMap<>(256));

    }

    public DefaultUserTokenManager(ConcurrentMap<String, SimpleUserToken> tokenStorage) {
        this(tokenStorage, new ConcurrentHashMap<>());
    }

    public DefaultUserTokenManager(ConcurrentMap<String, SimpleUserToken> tokenStorage, ConcurrentMap<String, Set<String>> userStorage) {
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

    private SimpleUserToken checkTimeout(SimpleUserToken detail) {
        if (null == detail) {
            return null;
        }
        if (detail.getMaxInactiveInterval() <= 0) {
            return detail;
        }
        if (System.currentTimeMillis() - detail.getLastRequestTime() > detail.getMaxInactiveInterval()) {
            changeTokenState(detail, TokenState.expired);
            return detail;
        }
        return detail;
    }

    @Override
    public SimpleUserToken getByToken(String token) {
        if (token == null) {
            return null;
        }
        return checkTimeout(tokenStorage.get(token));
    }

    @Override
    public List<UserToken> getByUserId(String userId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        Set<String> tokens = getUserToken(userId);
        if (tokens.isEmpty()) {
            userStorage.remove(userId);
            return new ArrayList<>();
        }
        return tokens
                .stream()
                .map(tokenStorage::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userIsLoggedIn(String userId) {
        if (userId == null) {
            return false;
        }
        for (UserToken userToken : getByUserId(userId)) {
            if (userToken.isNormal()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean tokenIsLoggedIn(String token) {
        if (token == null) {
            return false;
        }
        UserToken userToken = getByToken(token);

        return userToken != null && !userToken.isExpired();
    }

    @Override
    public long totalUser() {
        return userStorage.size();
    }

    @Override
    public long totalToken() {
        return tokenStorage.size();
    }

    @Override
    public void allLoggedUser(Consumer<UserToken> consumer) {
        tokenStorage.values().forEach(consumer);
    }

    @Override
    public List<UserToken> allLoggedUser() {
        return new ArrayList<>(tokenStorage.values());
    }

    @Override
    public void signOutByUserId(String userId) {
        if (null == userId) {
            return;
        }
        Set<String> tokens = getUserToken(userId);
        tokens.forEach(token -> signOutByToken(token, false));
        tokens.clear();
        userStorage.remove(userId);
    }

    private void signOutByToken(String token, boolean removeUserToken) {
        if (token == null) {
            return;
        }
        SimpleUserToken tokenObject = tokenStorage.remove(token);
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
    public void signOutByToken(String token) {
        signOutByToken(token, true);
    }

    protected void publishEvent(ApplicationEvent event) {
        if (null != eventPublisher) {
            eventPublisher.publishEvent(event);
        }
    }

    public void changeTokenState(SimpleUserToken userToken, TokenState state) {
        if (null != userToken) {
            SimpleUserToken copy = userToken.copy();

            userToken.setState(state);
            syncToken(userToken);

            publishEvent(new UserTokenChangedEvent(copy, userToken));
        }
    }

    @Override
    public void changeTokenState(String token, TokenState state) {
        changeTokenState(getByToken(token), state);
    }

    @Override
    public void changeUserState(String user, TokenState state) {
        getByUserId(user).forEach(token -> changeTokenState(token.getToken(), state));
    }

    @Override
    public UserToken signIn(String token, String type, String userId, long maxInactiveInterval) {
        SimpleUserToken detail = new SimpleUserToken(userId, token);
        detail.setType(type);
        detail.setMaxInactiveInterval(maxInactiveInterval);
        AllopatricLoginMode mode = allopatricLoginModes.getOrDefault(type, allopatricLoginMode);
        if (mode == AllopatricLoginMode.deny) {
            boolean hasAnotherToken = getByUserId(userId)
                    .stream()
                    .filter(userToken -> type.equals(userToken.getType()))
                    .map(SimpleUserToken.class::cast)
                    .peek(this::checkTimeout)
                    .anyMatch(UserToken::isNormal);
            if (hasAnotherToken) {
                throw new AccessDenyException("该用户已在其他地方登陆");
            }
        } else if (mode == AllopatricLoginMode.offlineOther) {
            //将在其他地方登录的用户设置为离线
            List<UserToken> oldToken = getByUserId(userId);
            for (UserToken userToken : oldToken) {
                //相同的tokenType才让其下线
                if (type.equals(userToken.getType())) {
                    changeTokenState(userToken.getToken(), TokenState.offline);
                }
            }
        }
        detail.setState(TokenState.normal);
        tokenStorage.put(token, detail);

        getUserToken(userId).add(token);

        publishEvent(new UserTokenCreatedEvent(detail));
        return detail;
    }

    @Override
    public void touch(String token) {
        SimpleUserToken userToken = tokenStorage.get(token);
        if (null != userToken) {
            userToken.touch();
            syncToken(userToken);
        }
    }

    @Override
    public void checkExpiredToken() {
        for (SimpleUserToken token : tokenStorage.values()) {
            if (token != null && checkTimeout(token).isExpired()) {
                signOutByToken(token.getToken());
            }
        }
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
