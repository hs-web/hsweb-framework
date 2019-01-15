/*
 *
 *  * Copyright 2019 http://www.hswebframework.org
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

package org.hswebframework.web.authorization.exception;

import org.hswebframework.web.authorization.token.TokenState;

/**
 * 未授权异常
 *
 * @author zhouhao
 * @since 3.0
 */
public class UnAuthorizedException extends RuntimeException {
    private static final long serialVersionUID = 2422918455013900645L;

    private final TokenState state;

    public UnAuthorizedException() {
        this(TokenState.expired);
    }

    public UnAuthorizedException(TokenState state) {
        this(state.getText(), state);
    }

    public UnAuthorizedException(String message, TokenState state) {
        super(message);
        this.state = state;
    }

    public UnAuthorizedException(String message, TokenState state, Throwable cause) {
        super(message, cause);
        this.state = state;
    }

    public TokenState getState() {
        return state;
    }
}
