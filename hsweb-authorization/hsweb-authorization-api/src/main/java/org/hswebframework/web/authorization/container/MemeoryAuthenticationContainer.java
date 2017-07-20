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

import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.container.event.UserSignInEvent;
import org.hswebframework.web.authorization.listener.AuthorizationListenerDispatcher;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * 授权容器,用来操作所有已经授权的用户
 *
 * @author zhouhao
 * @since 3.0
 */
public class MemeoryAuthenticationContainer implements AuthenticationContainer {

    private ConcurrentMap<String, SimpleUserToken> tokenUserStorage = new ConcurrentHashMap<>(256);

    private AuthenticationManager authenticationManager;

    // timeout seconds
    private long timeout = 3600;

    private AuthorizationListenerDispatcher authorizationListenerDispatcher;

    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public void setAuthorizationListenerDispatcher(AuthorizationListenerDispatcher authorizationListenerDispatcher) {
        this.authorizationListenerDispatcher = authorizationListenerDispatcher;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeout() {
        return timeout;
    }

    private UserToken checkTimeout(UserToken detail) {
        if (null == detail) return null;
        if (System.currentTimeMillis() - detail.getLastRequestTime() > timeout * 1000) {
            logoutByToken(detail.getToken());
            return null;
        }
        return detail;
    }

    @Override
    public UserToken getByToken(String token) {
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
        return getByUserId(userId).size() > 0;
    }

    @Override
    public boolean tokenIsLoggedIn(String token) {
        return getByToken(token) != null;
    }

    @Override
    public int totalUser() {
        return tokenUserStorage.values().stream().map(UserToken::getUserId).distinct().mapToInt(userId->1).sum();
    }

    @Override
    public int totalToken() {
        return tokenUserStorage.size();
    }

    @Override
    public List<UserToken> allLoggedUser() {
        return tokenUserStorage.values().stream()
                .map(this::checkTimeout)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public void logoutByUserId(String userId) {
        getByUserId(userId).forEach(detail -> logoutByToken(detail.getToken()));
    }

    @Override
    public void logoutByToken(String token) {
        tokenUserStorage.remove(token);
    }

    @Override
    public UserToken signIn(String token, String userId) {
        SimpleUserToken detail = new SimpleUserToken(userId, token);
        if (null != authorizationListenerDispatcher)
            authorizationListenerDispatcher.doEvent(new UserSignInEvent(detail));
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
