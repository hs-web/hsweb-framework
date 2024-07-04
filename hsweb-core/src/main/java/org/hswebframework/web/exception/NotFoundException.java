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

package org.hswebframework.web.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends BusinessException {
    public NotFoundException(String message, Object... args) {
        super(message, 404, args);
    }

    public NotFoundException() {
        this("error.not_found");
    }

    /**
     * 不填充线程栈的异常，在一些对线程栈不敏感，且对异常不可控（如: 来自未认证请求产生的异常）的情况下不填充线程栈对性能有利。
     */
    public static class NoStackTrace extends NotFoundException {
        public NoStackTrace(String code, Object... args) {
            super(code, args);
        }

        public NoStackTrace() {
            super();
        }

        @Override
        public final synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
