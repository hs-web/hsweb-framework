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

package org.hswebframework.web.authorization.oauth2.server.client;

import java.util.Set;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface OAuth2Client {
    String getId();

    String getSecret();

    String getName();

    String getRedirectUri();

    String getOwnerId();

    Long getCreateTime();

    /**
     * @return 状态
     * @see org.hswebframework.web.commons.entity.DataStatus
     */
    Byte getStatus();

    /**
     * @return 客户端支持的认证类型
     * @see org.hswebframework.web.oauth2.core.GrantType
     */
    Set<String> getSupportGrantTypes();

    Set<String> getDefaultGrantScope();

    default boolean isSupportGrantType(String grantType) {
        Set<String> supports = getSupportGrantTypes();
        return supports != null && (supports.contains(grantType) || supports.contains("*"));
    }
}
