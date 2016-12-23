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

import org.apache.shiro.subject.Subject;
import org.hswebframework.web.authorization.UserSubject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author zhouhao
 */
public class ShiroUserSubject implements UserSubject {
    private Subject subject;

    public ShiroUserSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public String getUsername() {
        return (String) subject.getPrincipal();
    }

    @Override
    public boolean hasRoles(Set<String> roles) {
        return subject.hasAllRoles(roles);
    }

    @Override
    public boolean hasRole(String... roleId) {
        return hasRoles(new HashSet<>(Arrays.asList(roleId)));
    }

    @Override
    public boolean hasModule(String moduleId, String... actions) {
        if (actions.length == 0) return subject.isPermitted(moduleId);
        String per[] = new String[actions.length];
        for (int i = 0; i < per.length; i++) {
            per[i] = moduleId.concat(":").concat(actions[i]);
        }
        return subject.isPermittedAll(per);
    }
}
