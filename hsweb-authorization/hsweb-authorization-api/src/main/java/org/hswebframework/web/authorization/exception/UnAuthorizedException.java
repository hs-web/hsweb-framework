/*
 *
 *  * Copyright 2020 http://www.hswebframework.org
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

import lombok.Getter;
import org.hswebframework.web.authorization.token.TokenState;
import org.hswebframework.web.exception.I18nSupportException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 未授权异常
 *
 * @author zhouhao
 * @since 3.0
 */
@Getter
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnAuthorizedException extends I18nSupportException {
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

    /**
     * 不填充线程栈的异常，在一些对线程栈不敏感，且对异常不可控（如: 来自未认证请求产生的异常）的情况下不填充线程栈对性能有利。
     */
    public static class NoStackTrace extends UnAuthorizedException {
        public NoStackTrace() {
            super();
        }

        public NoStackTrace(TokenState state) {
            super(state);
        }

        public NoStackTrace(String message, TokenState state) {
            super(message, state);
        }

        public NoStackTrace(String message, TokenState state, Throwable cause) {
            super(message, state, cause);
        }

        @Override
        public final synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
