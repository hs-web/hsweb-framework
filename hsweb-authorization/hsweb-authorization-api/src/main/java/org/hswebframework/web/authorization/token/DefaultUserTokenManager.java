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
import java.util.stream.Collectors;

/**
 * 授权容器,用来操作所有已经授权的用户
 *
 * @author zhouhao
 * @since 3.0
 */
public class DefaultUserTokenManager implements UserTokenManager {

    protected final ConcurrentMap<String, SimpleUserToken> tokenUserStorage;

    protected ConcurrentMap<String, List<String>> userStorage;

    public DefaultUserTokenManager() {
        this(new ConcurrentHashMap<>(256));

    }

    public DefaultUserTokenManager(ConcurrentMap<String, SimpleUserToken> storage) {
        this(storage, new ConcurrentHashMap<>());
    }

    public DefaultUserTokenManager(ConcurrentMap<String, SimpleUserToken> storage, ConcurrentMap<String, List<String>> userStorage) {
        this.tokenUserStorage = storage;
        this.userStorage = userStorage;

    }

    //令牌超时事件,默认3600秒
    private long timeout = 3600;

    //异地登录模式，默认允许异地登录
    private AllopatricLoginMode allopatricLoginMode = AllopatricLoginMode.allow;

    //事件转发器
    private ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    public void setEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setAllopatricLoginMode(AllopatricLoginMode allopatricLoginMode) {
        this.allopatricLoginMode = allopatricLoginMode;
    }

    public AllopatricLoginMode getAllopatricLoginMode() {
        return allopatricLoginMode;
    }

    protected List<String> getUserToken(String userId) {
        return userStorage.computeIfAbsent(userId, key -> new ArrayList<>());
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
        return checkTimeout(tokenUserStorage.get(token));
    }

    @Override
    public List<UserToken> getByUserId(String userId) {
        return getUserToken(userId)
                .stream()
                .map(tokenUserStorage::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userIsLoggedIn(String userId) {
        for (UserToken userToken : getByUserId(userId)) {
            if (userToken.isEffective()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean tokenIsLoggedIn(String token) {
        UserToken userToken = getByToken(token);

        return userToken != null && !userToken.isExpired();
    }

    @Override
    public long totalUser() {
        return userStorage.size();

//        return tokenUserStorage.values()
//                .parallelStream()
//                .peek(this::checkTimeout)//检查是否已经超时
//                .filter(UserToken::isEffective)//只返回有效的
//                .map(UserToken::getUserId)
//                .distinct()//去重复
//                .count();
    }

    @Override
    public long totalToken() {
        return tokenUserStorage.size();
    }

    @Override
    public void allLoggedUser(Consumer<UserToken> consumer) {
        tokenUserStorage.values().forEach(consumer);
    }

    @Override
    public List<UserToken> allLoggedUser() {
        return new ArrayList<>(tokenUserStorage.values());
    }

    @Override
    public void signOutByUserId(String userId) {
        if (null == userId) {
            return;
        }
        List<String> tokens =  getUserToken(userId);
        tokens.forEach(token->signOutByToken(token,false));
        tokens.clear();
        userStorage.remove(userId);
    }

    private void signOutByToken(String token,boolean removeUserToken) {
        SimpleUserToken tokenObject = tokenUserStorage.remove(token);
        if (tokenObject != null) {
            String userId = tokenObject.getUserId();
            if(removeUserToken) {
                List<String> tokens = getUserToken(userId);
                if (tokens.size() > 0) {
                    tokens.remove(token);
                }
                if (tokens.size() == 0) {
                    userStorage.remove(tokenObject.getUserId());
                }
            }
            publishEvent(new UserTokenRemovedEvent(tokenObject));
        }
    }

    @Override
    public void signOutByToken(String token) {
       signOutByToken(token,true);
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

        if (allopatricLoginMode == AllopatricLoginMode.deny) {
            changeTokenState(detail.getToken(), TokenState.deny);
        } else if (allopatricLoginMode == AllopatricLoginMode.offlineOther) {
            detail.setState(TokenState.effective);
            //将已经登录的用户设置为离线
            List<UserToken> oldToken = getByUserId(userId);
            for (UserToken userToken : oldToken) {
                changeTokenState(userToken.getToken(), TokenState.offline);
            }
        } else {
            detail.setState(TokenState.effective);
        }
        tokenUserStorage.put(token, detail);

        getUserToken(userId).add(token);

        publishEvent(new UserTokenCreatedEvent(detail));
        return detail;
    }

    @Override
    public void touch(String token) {
        SimpleUserToken userToken = tokenUserStorage.get(token);
        if (null != userToken) {
            userToken.touch();
            syncToken(userToken);
        }
    }

    @Override
    public void checkExpiredToken() {
        for (SimpleUserToken token : tokenUserStorage.values()) {
            checkTimeout(token);
            if (token.isExpired()) {
                signOutByToken(token.getToken());
            }
        }
    }

    protected void syncToken(UserToken userToken) {
        //do noting
    }
}
