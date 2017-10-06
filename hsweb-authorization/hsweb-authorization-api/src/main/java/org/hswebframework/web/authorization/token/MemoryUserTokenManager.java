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

import org.hswebframework.web.authorization.listener.AuthorizationListenerDispatcher;
import org.hswebframework.web.authorization.token.event.UserSignInEvent;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 授权容器,用来操作所有已经授权的用户
 *
 * @author zhouhao
 * @since 3.0
 */
public class MemoryUserTokenManager implements UserTokenManager {

    private final ConcurrentMap<String, SimpleUserToken> tokenUserStorage = new ConcurrentHashMap<>(256);

    //令牌超时事件,默认3600秒
    private long timeout = 3600;

    //异地登录模式，默认允许异地登录
    private AllopatricLoginMode allopatricLoginMode = AllopatricLoginMode.allow;

    //事件转发器
    private AuthorizationListenerDispatcher authorizationListenerDispatcher;

    public void setAuthorizationListenerDispatcher(AuthorizationListenerDispatcher authorizationListenerDispatcher) {
        this.authorizationListenerDispatcher = authorizationListenerDispatcher;
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

    private SimpleUserToken checkTimeout(SimpleUserToken detail) {
        if (null == detail) return null;
        if (detail.getMaxInactiveInterval() <= 0) {
            return detail;
        }
        if (System.currentTimeMillis() - detail.getLastRequestTime() > detail.getMaxInactiveInterval()) {
            detail.setState(TokenState.expired);
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
        return tokenUserStorage.values().stream()
                .filter(detail -> detail.getUserId().equals(userId) && checkTimeout(detail) != null)
                .collect(Collectors.toList());
    }

    @Override
    public boolean userIsLoggedIn(String userId) {
        for (UserToken userToken : getByUserId(userId)) {
            if (userToken.isEffective()) return true;
        }
        return false;
    }

    @Override
    public boolean tokenIsLoggedIn(String token) {

        return getByToken(token) != null;
    }

    @Override
    public long totalUser() {
        return tokenUserStorage.values()
                .stream()
                .peek(this::checkTimeout)//检查是否已经超时
                .filter(UserToken::isEffective)//只返回有效的
                .map(UserToken::getUserId)
                .distinct()//去重复
                .count();
    }

    @Override
    public long totalToken() {
        return tokenUserStorage.values()
                .stream()
                .peek(this::checkTimeout)//检查是否已经超时
                .filter(UserToken::isEffective)//只返回有效的
                .count();
    }

    @Override
    public List<UserToken> allLoggedUser() {
        return tokenUserStorage.values()
                .stream()
                .map(this::checkTimeout)
                .filter(UserToken::isEffective)
                .collect(Collectors.toList());
    }

    @Override
    public void signOutByUserId(String userId) {
        getByUserId(userId).forEach(detail -> signOutByToken(detail.getToken()));
    }

    @Override
    public void signOutByToken(String token) {
        tokenUserStorage.remove(token);
    }

    @Override
    public void changeTokenState(String token, TokenState state) {
        SimpleUserToken userToken = getByToken(token);
        if (null != userToken)
            userToken.setState(state);
    }

    @Override
    public void changeUserState(String user, TokenState state) {
        getByUserId(user).forEach(token -> changeTokenState(token.getToken(), state));
    }

    @Override
    public UserToken signIn(String token, String userId, long maxInactiveInterval) {
        SimpleUserToken detail = new SimpleUserToken(userId, token);
        if (null != authorizationListenerDispatcher)
            authorizationListenerDispatcher.doEvent(new UserSignInEvent(detail));
        if (allopatricLoginMode == AllopatricLoginMode.deny) {
            detail.setState(TokenState.deny);
        } else if (allopatricLoginMode == AllopatricLoginMode.offlineOther) {
            detail.setState(TokenState.effective);
            SimpleUserToken oldToken = (SimpleUserToken) getByUserId(userId);
            if (oldToken != null) {
                //踢下线
                oldToken.setState(TokenState.offline);
            }
        } else {
            detail.setState(TokenState.effective);
        }
        detail.setMaxInactiveInterval(maxInactiveInterval);
        tokenUserStorage.put(token, detail);
        return detail;
    }

    @Override
    public void touch(String token) {
        SimpleUserToken detail = tokenUserStorage.get(token);
        if (null != detail)
            detail.touch();
    }

}
