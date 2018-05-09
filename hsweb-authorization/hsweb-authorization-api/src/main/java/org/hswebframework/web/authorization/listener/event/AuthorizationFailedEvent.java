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

package org.hswebframework.web.authorization.listener.event;

import java.util.function.Function;

/**
 * 授权失败时触发
 *
 * @author zhouhao
 */
public class AuthorizationFailedEvent extends AbstractAuthorizationEvent {

    private static final long serialVersionUID = -101792832265740828L;
    /**
     * 失败原因
     */
    private Reason reason;

    /**
     * 异常信息
     */
    private Exception exception;

    public AuthorizationFailedEvent(String username,
                                    String password,
                                    Function<String, Object> parameterGetter,
                                    Reason reason) {
        super(username, password, parameterGetter);
        this.reason = reason;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        PASSWORD_ERROR,
        USER_DISABLED,
        USER_NOT_EXISTS,
        OTHER
    }
}
