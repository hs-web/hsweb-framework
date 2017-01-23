/*
 *
 *  * Copyright 2016 http://www.hswebframework.org
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.hswebframework.web.authorization.shiro;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.subject.Subject;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author zhouhao
 */
public class ShiroTests {

    @Test
    public void simpleTest() {
        DefaultSessionManager sessionManager = new DefaultSessionManager();
        DefaultSecurityManager securityManager = new DefaultSecurityManager();

        securityManager.setAuthenticator(new ModularRealmAuthenticator());
        SimpleAccountRealm realm = new SimpleAccountRealm();
        realm.addAccount("admin", "admin", "admin");

        securityManager.setRealm(realm);
        securityManager.setSessionManager(sessionManager);
        SecurityUtils.setSecurityManager(securityManager);
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken("admin", "admin");

        token.setRememberMe(true);
        subject.login(token);
        Assert.assertTrue(subject.isAuthenticated());
        Assert.assertTrue(subject.hasRole("admin"));
        Assert.assertFalse(subject.hasRole("test"));

        System.out.println();
    }
}
