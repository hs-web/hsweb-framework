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

package org.hswebframework.web.authorization.oauth2.client.simple.request.builder;

import org.hswebframework.expands.request.RequestBuilder;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestBuilder;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Request;
import org.hswebframework.web.authorization.oauth2.client.request.ResponseConvertHandler;
import org.hswebframework.web.authorization.oauth2.client.request.ResponseJudge;
import org.hswebframework.web.authorization.oauth2.client.simple.request.SimpleOAuth2Request;

/**
 * @author zhouhao
 */
public class SimpleOAuth2RequestBuilder implements OAuth2RequestBuilder {

    private RequestBuilder requestBuilder;

    private String url;

    private ResponseConvertHandler convertHandler;

    private ResponseJudge responseJudge;

    public SimpleOAuth2RequestBuilder requestBuilder(RequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
        return this;
    }

    public SimpleOAuth2RequestBuilder convertHandler(ResponseConvertHandler convertHandler) {
        this.convertHandler = convertHandler;
        return this;
    }

    public SimpleOAuth2RequestBuilder responseJudge(ResponseJudge responseJudge) {
        this.responseJudge = responseJudge;
        return this;
    }

    @Override
    public SimpleOAuth2RequestBuilder url(String url) {
        this.url = url;
        return this;
    }

    @Override
    public OAuth2Request build() {
        SimpleOAuth2Request request = new SimpleOAuth2Request(url.startsWith("https:") ? requestBuilder.https(url) : requestBuilder.http(url));
        request.setConvertHandler(convertHandler);
        request.setResponseJudge(responseJudge);
        return request;
    }
}
