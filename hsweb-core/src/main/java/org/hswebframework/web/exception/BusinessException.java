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

import lombok.Getter;

/**
 * 业务异常
 *
 * @author zhouhao
 * @since 2.0
 */
@Getter
public class BusinessException extends I18nSupportException {
    private static final long serialVersionUID = 5441923856899380112L;

    private int status = 500;
    private String code;

    public BusinessException(String message) {
        this(message, 500);
    }

    public BusinessException(String message, int status, Object... args) {
        this(message, null, status, args);
    }

    public BusinessException(String message, String code) {
        this(message, code, 500);
    }


    public BusinessException(String message, String code, int status, Object... args) {
        super(message, args);
        this.code = code;
        this.status = status;
    }


    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message, Throwable cause, int status) {
        super(message, cause);
        this.status = status;
    }

    /**
     * 不填充线程栈的异常，在一些对线程栈不敏感，且对异常不可控（如: 来自未认证请求产生的异常）的情况下不填充线程栈对性能有利。
     */
    public static class NoStackTrace extends BusinessException {
        public NoStackTrace(String message) {
            this(message, 500);
        }

        public NoStackTrace(String message, int status, Object... args) {
            this(message, null, status, args);
        }

        public NoStackTrace(String message, String code) {
            this(message, code, 500);
        }

        public NoStackTrace(String message, String code, int status, Object... args) {
            super(message, code, status, args);

        }

        public NoStackTrace(String message, Throwable cause) {
            super(message, cause);
        }

        public NoStackTrace(String message, Throwable cause, int status) {
            super(message, cause, status);
        }


        @Override
        public final synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}
