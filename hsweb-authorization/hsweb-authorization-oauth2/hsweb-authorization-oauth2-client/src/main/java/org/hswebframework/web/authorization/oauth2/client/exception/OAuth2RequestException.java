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

package org.hswebframework.web.authorization.oauth2.client.exception;

import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.oauth2.core.ErrorType;

import java.io.PrintStream;

/**
 * @author zhouhao
 */
public class OAuth2RequestException extends RuntimeException {
    private static final long serialVersionUID = 6170266627415485170L;
    private ErrorType errorType;

    private OAuth2Response response;

    public OAuth2RequestException(ErrorType errorType, OAuth2Response response) {
        super(errorType.name() + (errorType == ErrorType.OTHER ? ":" + response.asString() : ""));
        this.errorType = errorType;
        this.response = response;
    }

    public OAuth2RequestException(String message, ErrorType errorType, OAuth2Response response) {
        super(errorType+":"+message);
        this.errorType = errorType;
        this.response = response;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public OAuth2Response getResponse() {
        return response;
    }

}
