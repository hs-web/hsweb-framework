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

package org.hswebframework.web.service.oauth2.client.simple.session;

import org.apache.commons.codec.binary.Base64;
import org.hswebframework.web.authorization.oauth2.client.AccessTokenInfo;
import org.hswebframework.web.authorization.oauth2.client.GrantType;
import org.hswebframework.web.authorization.oauth2.client.OAuth2Constants;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestBuilderFactory;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Request;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.entity.oauth2.client.OAuth2ServerConfigEntity;
import org.springframework.util.Assert;

import java.util.function.Consumer;

import static org.hswebframework.web.authorization.oauth2.client.OAuth2Constants.*;

/**
 * @author zhouhao
 */
public class DefaultOAuth2Session implements OAuth2Session {

    protected OAuth2RequestBuilderFactory requestBuilderFactory;

    protected OAuth2ServerConfigEntity configEntity;

    protected boolean closed = false;

    protected OAuth2Request accessTokenRequest;

    protected AccessTokenInfo accessTokenInfo;

    protected String scope;

    private Consumer<AccessTokenInfo> onTokenChange;

    public void setRequestBuilderFactory(OAuth2RequestBuilderFactory requestBuilderFactory) {
        this.requestBuilderFactory = requestBuilderFactory;
    }

    public void setConfigEntity(OAuth2ServerConfigEntity configEntity) {
        this.configEntity = configEntity;
    }

    public void init() {
        Assert.notNull(requestBuilderFactory, "requestBuilderFactory can not be null!");
        Assert.notNull(configEntity, "configEntity can not be null!");
        accessTokenRequest = createRequest(configEntity.getAccessTokenUrl());
        applyBasicAuthParam(accessTokenRequest);
    }

    protected OAuth2Request createRequest(String uriOrUrl) {
        return requestBuilderFactory
                .create(configEntity.getId(), configEntity.getProvider())
                .url(getRealUrl(uriOrUrl))
                .build();
    }

    public void onTokenChanged(Consumer<AccessTokenInfo> changed) {
        onTokenChange = changed;
    }

    protected String encodeAuthorization(String auth) {
        return "basic ".concat(Base64.encodeBase64String(auth.getBytes()));
    }

    protected void applyBasicAuthParam(OAuth2Request request) {
        request.param(client_id, configEntity.getClientId());
        request.param(client_secret, configEntity.getClientSecret());
        request.param(redirect_uri, configEntity.getRedirectUri());
        request.header(authorization, encodeAuthorization(configEntity.getClientId().concat(":").concat(configEntity.getClientSecret())));
    }

    protected void applyTokenParam(OAuth2Request request) {
        request.param(access_token, getAccessToken().getAccessToken());
        request.header(authorization, "Bearer " + getAccessToken().getAccessToken());
    }

    protected String getRealUrl(String url) {
        if (url.startsWith("http")) return url;
        if (!configEntity.getApiBaseUrl().endsWith("/") && !url.startsWith("/"))
            return configEntity.getApiBaseUrl().concat("/").concat(url);
        return configEntity.getApiBaseUrl() + url;
    }

    @Override
    public OAuth2Session authorize() {
        AccessTokenInfo accessTokenInfo = accessTokenRequest
                .param(OAuth2Constants.scope, scope)
                .post().onError(OAuth2Response.throwOnError)
                .as(AccessTokenInfo.class);
        accessTokenInfo.setCreateTime(System.currentTimeMillis());
        setAccessTokenInfo(accessTokenInfo);
        return this;
    }

    @Override
    public OAuth2Request request(String uriOrUrl) {
        if (accessTokenInfo == null) authorize();
        OAuth2Request request = createRequest(getRealUrl(uriOrUrl));
        request.onTokenExpired(retry -> {
            refreshToken(); //刷新token
            applyTokenParam(request); //重设请求参数
            retry.doReTry(); //执行重试
        });
        applyTokenParam(request);
        return request;
    }

    @Override
    public OAuth2Session param(String name, Object value) {
        accessTokenRequest.param(name, String.valueOf(value));
        return this;
    }

    protected void refreshToken() {
        if (accessTokenInfo == null) return;
        OAuth2Request request = createRequest(getRealUrl(configEntity.getAccessTokenUrl()));
        applyBasicAuthParam(request);
        AccessTokenInfo tokenInfo = request
                .param(OAuth2Constants.scope, scope)
                .param(OAuth2Constants.grant_type, GrantType.refresh_token)
                .param(GrantType.refresh_token, accessTokenInfo.getRefreshToken())
                .post().onError(OAuth2Response.throwOnError)
                .as(AccessTokenInfo.class);
        tokenInfo.setCreateTime(accessTokenInfo.getCreateTime());
        tokenInfo.setUpdateTime(System.currentTimeMillis());
        setAccessTokenInfo(tokenInfo);
    }


    @Override
    public OAuth2Session scope(String scope) {
        this.scope = scope;
        return this;
    }

    @Override
    public void close() {
        closed = true;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public AccessTokenInfo getAccessToken() {
        if (accessTokenInfo == null) return null;
        if (accessTokenInfo.isExpire()) refreshToken();
        return accessTokenInfo;
    }

    private void setAccessTokenInfo(AccessTokenInfo accessTokenInfo) {
        this.accessTokenInfo = accessTokenInfo;
        if (onTokenChange != null) onTokenChange.accept(accessTokenInfo);
    }
}
