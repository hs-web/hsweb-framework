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

package org.hswebframework.web.authorization.shiro.remember;

import org.apache.shiro.authc.*;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.subject.WebSubject;
import org.apache.shiro.web.subject.WebSubjectContext;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleRememberMeManager implements RememberMeManager {

    public static final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "rememberMe";

    private Cookie cookie;

    private RememberStorage rememberStorage = new DefaultRememberStorage();

    public SimpleRememberMeManager() {
        Cookie cookie = new SimpleCookie(DEFAULT_REMEMBER_ME_COOKIE_NAME);
        cookie.setHttpOnly(true);
        //One year should be long enough - most sites won't object to requiring a user to log in if they haven't visited
        //in a year:
        cookie.setMaxAge(Cookie.ONE_YEAR);
        this.cookie = cookie;
    }

    public void setRememberStorage(RememberStorage rememberStorage) {
        this.rememberStorage = rememberStorage;
    }

    public Cookie getCookie() {
        return cookie;
    }

    public void setCookie(Cookie cookie) {
        this.cookie = cookie;
    }

    protected String getRememberKey(SubjectContext context) {
        if (!(context instanceof WebSubjectContext)) return null;
        WebSubjectContext webSubjectContext = ((WebSubjectContext) context);
        HttpServletRequest request = WebUtils.getHttpRequest(webSubjectContext);
        HttpServletResponse response = WebUtils.getHttpResponse(webSubjectContext);
        return getCookie().readValue(request, response);
    }

    @Override
    public PrincipalCollection getRememberedPrincipals(SubjectContext subjectContext) {
        String key = getRememberKey(subjectContext);
        if (null == key) return null;
        RememberInfo info = rememberStorage.get(key);
        if (info == null) return null;
        return info.getPrincipal();
    }

    @Override
    public void forgetIdentity(SubjectContext subjectContext) {
        String key = getRememberKey(subjectContext);
        if (null == key) return;
        rememberStorage.remove(key);
    }

    @Override
    public void onSuccessfulLogin(Subject subject, AuthenticationToken token, AuthenticationInfo info) {
        if (!(subject instanceof WebSubject)) return;
        if (!(token instanceof RememberMeAuthenticationToken) || !((RememberMeAuthenticationToken) token).isRememberMe()) return;
        PrincipalCollection principalCollection = info.getPrincipals();
        HttpServletRequest request = WebUtils.getHttpRequest(subject);
        HttpServletResponse response = WebUtils.getHttpResponse(subject);

        getCookie().removeFrom(request, response);
        RememberInfo rememberInfo = rememberStorage.create(principalCollection);
        SimpleCookie simpleCookie = new SimpleCookie(getCookie());
        simpleCookie.setValue(rememberInfo.getKey());
        simpleCookie.saveTo(request, response);
        rememberStorage.put(rememberInfo);
    }

    @Override
    public void onFailedLogin(Subject subject, AuthenticationToken token, AuthenticationException ae) {
        if (!(subject instanceof WebSubject)) return;
        HttpServletRequest request = WebUtils.getHttpRequest(subject);
        HttpServletResponse response = WebUtils.getHttpResponse(subject);
        getCookie().removeFrom(request, response);
    }

    @Override
    public void onLogout(Subject subject) {
        if (!(subject instanceof WebSubject)) return;
        HttpServletRequest request = WebUtils.getHttpRequest(subject);
        HttpServletResponse response = WebUtils.getHttpResponse(subject);
        getCookie().removeFrom(request, response);
    }
}
