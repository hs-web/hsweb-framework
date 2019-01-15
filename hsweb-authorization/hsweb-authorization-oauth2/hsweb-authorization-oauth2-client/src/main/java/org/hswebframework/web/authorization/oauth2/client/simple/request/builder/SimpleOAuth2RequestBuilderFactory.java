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
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestBuilderFactory;
import org.hswebframework.web.authorization.oauth2.client.request.ResponseConvertHandler;
import org.hswebframework.web.authorization.oauth2.client.request.ResponseJudge;
import org.hswebframework.web.authorization.oauth2.client.request.definition.ResponseConvertForProviderDefinition;
import org.hswebframework.web.authorization.oauth2.client.request.definition.ResponseConvertForServerIdDefinition;
import org.hswebframework.web.authorization.oauth2.client.request.definition.ResponseJudgeForProviderDefinition;
import org.hswebframework.web.authorization.oauth2.client.request.definition.ResponseJudgeForServerIdDefinition;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhouhao
 */
public class SimpleOAuth2RequestBuilderFactory implements OAuth2RequestBuilderFactory, BeanPostProcessor {

    private final Map<String, ResponseJudge>          judgeMap          = new HashMap<>();

    private final Map<String, ResponseConvertHandler> convertHandlerMap = new HashMap<>();

    ResponseConvertHandler defaultConvertHandler;

    ResponseJudge defaultResponseJudge;

    RequestBuilder requestBuilder;

    public void setRequestBuilder(RequestBuilder requestBuilder) {
        this.requestBuilder = requestBuilder;
    }

    public RequestBuilder getRequestBuilder() {
        return requestBuilder;
    }

    public void setJudgeForServerId(String serverId, ResponseJudge judge) {
        judgeMap.put("serverId:" + serverId, judge);
    }

    public void setConvertForServerId(String serverId, ResponseConvertHandler convertHandler) {
        convertHandlerMap.put("serverId:" + serverId, convertHandler);
    }

    public void setJudgeForProvider(String serverId, ResponseJudge judge) {
        judgeMap.put("provider:" + serverId, judge);
    }

    public void setConvertForProvider(String serverId, ResponseConvertHandler convertHandler) {
        convertHandlerMap.put("provider:" + serverId, convertHandler);
    }

    public void setDefaultConvertHandler(ResponseConvertHandler defaultConvertHandler) {
        this.defaultConvertHandler = defaultConvertHandler;
    }

    public void setDefaultResponseJudge(ResponseJudge defaultResponseJudge) {
        this.defaultResponseJudge = defaultResponseJudge;
    }

    protected ResponseConvertHandler getConvertHandler(String id, String provider) {
        ResponseConvertHandler convertHandler = convertHandlerMap.get("serverId:" + id);
        if (convertHandler == null) {
            convertHandler = convertHandlerMap.getOrDefault("provider:" + provider, defaultConvertHandler);
        }
        return convertHandler;
    }

    protected ResponseJudge getResponseJudge(String id, String provider) {
        ResponseJudge judge = judgeMap.get("serverId:" + id);
        if (judge == null) {
            judge = judgeMap.getOrDefault("provider:" + provider, defaultResponseJudge);
        }
        return judge;
    }

    @Override
    public OAuth2RequestBuilder create(String serverId, String provider) {
        SimpleOAuth2RequestBuilder builder = new SimpleOAuth2RequestBuilder();
        builder.requestBuilder(getRequestBuilder())
                .convertHandler(getConvertHandler(serverId, provider))
                .responseJudge(getResponseJudge(serverId, provider));
        return builder;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof ResponseJudgeForServerIdDefinition) {
            ResponseJudgeForServerIdDefinition definition = ((ResponseJudgeForServerIdDefinition) bean);
            setJudgeForServerId(definition.getServerId(), definition);
        }
        if (bean instanceof ResponseConvertForServerIdDefinition) {
            ResponseConvertForServerIdDefinition definition = ((ResponseConvertForServerIdDefinition) bean);
            setConvertForServerId(definition.getServerId(), definition);
        }
        if (bean instanceof ResponseJudgeForProviderDefinition) {
            ResponseJudgeForProviderDefinition definition = ((ResponseJudgeForProviderDefinition) bean);
            setJudgeForProvider(definition.getProvider(), definition);
        }
        if (bean instanceof ResponseConvertForProviderDefinition) {
            ResponseConvertForProviderDefinition definition = ((ResponseConvertForProviderDefinition) bean);
            setConvertForProvider(definition.getProvider(), definition);
        }
        return bean;
    }
}
