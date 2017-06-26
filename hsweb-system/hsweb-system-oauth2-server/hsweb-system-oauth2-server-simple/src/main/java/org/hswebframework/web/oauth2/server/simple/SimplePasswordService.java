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

package org.hswebframework.web.oauth2.server.simple;

import org.hswebframework.web.authorization.oauth2.server.support.password.PasswordService;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.service.authorization.UserService;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimplePasswordService implements PasswordService {
    private UserService userService;

    public SimplePasswordService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public String getUserIdByUsernameAndPassword(String username, String password) {
        UserEntity userEntity = userService.selectByUsername(username);
        if (userEntity == null) return null;
        if (!userService.encodePassword(password, userEntity.getSalt()).equals(userEntity.getPassword())) {
            return null;
        }
        return userEntity.getId();
    }
}
