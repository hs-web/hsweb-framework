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

package org.hswebframework.web.authorization.oauth2.server.entity;

import org.hswebframework.web.commons.entity.SimpleGenericEntity;

import java.util.Set;

/**
 * @author zhouhao
 */
public class SimpleOAuth2ClientEntity extends SimpleGenericEntity<String> implements OAuth2ClientEntity {
    private String name;

    private String secret;

    private String redirectUri;

    private String ownerId;

    private String creatorId;

    private Long createTime;

    private String type;

    private String describe;

    private Set<String> supportGrantTypes;

    private Set<String> defaultGrantScope;

    private Byte status;

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    @Override
    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public String getCreatorId() {
        return creatorId;
    }

    @Override
    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    @Override
    public Long getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Set<String> getSupportGrantTypes() {
        return supportGrantTypes;
    }

    @Override
    public void setSupportGrantTypes(Set<String> supportGrantType) {
        this.supportGrantTypes = supportGrantType;
    }

    @Override
    public Set<String> getDefaultGrantScope() {
        return defaultGrantScope;
    }

    @Override
    public void setDefaultGrantScope(Set<String> defaultGrantScope) {
        this.defaultGrantScope = defaultGrantScope;
    }

    @Override
    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }
}
