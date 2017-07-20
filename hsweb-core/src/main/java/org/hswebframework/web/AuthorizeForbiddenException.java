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

package org.hswebframework.web;

/**
 * Created by 浩 on 2015-12-23 0023.
 */
public class AuthorizeForbiddenException extends BusinessException {
    private static final long serialVersionUID = 2422918455013900645L;

    public AuthorizeForbiddenException(String message) {
        this(message, 403);
    }

    public AuthorizeForbiddenException(String message, int status) {
        super(message, status);
    }

    public AuthorizeForbiddenException(String message, Throwable cause, int status) {
        super(message, cause, status);
    }
}
