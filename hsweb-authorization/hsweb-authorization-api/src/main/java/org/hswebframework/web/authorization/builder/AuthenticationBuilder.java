/*
 * Copyright 2016 http://www.hswebframework.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.hswebframework.web.authorization.builder;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.Role;
import org.hswebframework.web.authorization.User;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public interface AuthenticationBuilder extends Serializable {

    AuthenticationBuilder user(User user);

    AuthenticationBuilder user(String user);

    AuthenticationBuilder user(Map<String, String> user);


    AuthenticationBuilder role(List<Role> role);

    AuthenticationBuilder role(String role);


    AuthenticationBuilder permission(List<Permission> permission);

    AuthenticationBuilder permission(String permission);

    AuthenticationBuilder attributes(String attributes);

    AuthenticationBuilder attributes(Map<String, Serializable> permission);

    AuthenticationBuilder json(String json);

    Authentication build();

}
