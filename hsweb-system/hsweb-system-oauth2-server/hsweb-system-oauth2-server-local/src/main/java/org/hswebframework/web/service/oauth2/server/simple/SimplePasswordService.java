/*
 *  Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.service.oauth2.server.simple;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.AuthenticationManager;
import org.hswebframework.web.authorization.oauth2.server.support.password.PasswordService;
import org.hswebframework.web.authorization.simple.PlainTextUsernamePasswordAuthenticationRequest;
import org.hswebframework.web.validate.ValidationException;

/**
 * @author zhouhao
 */
public class SimplePasswordService implements PasswordService {
    private AuthenticationManager authenticationManager;

    public SimplePasswordService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public String getUserIdByUsernameAndPassword(String username, String password) {
        try {
            Authentication authenticate = authenticationManager.authenticate(new PlainTextUsernamePasswordAuthenticationRequest(username, password));
            if (null != authenticate) {
                return authenticate.getUser().getId();
            }
        } catch (ValidationException | UnsupportedOperationException | IllegalArgumentException e) {
            return null;
        }
        return null;
    }
}
