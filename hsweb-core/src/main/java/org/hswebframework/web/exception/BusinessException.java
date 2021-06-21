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
public class BusinessException extends I18nSupportException {
    private static final long serialVersionUID = 5441923856899380112L;

    @Getter
    private int status = 500;

    @Getter
    private String code;

    public BusinessException(String message) {
        this(message, 500);
    }

    public BusinessException(String message, int status, Object... args) {
        this(message, null, status, args);
    }

    public BusinessException(String message, String code, Object... args) {
        this(message, code, 500, args);
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
}
