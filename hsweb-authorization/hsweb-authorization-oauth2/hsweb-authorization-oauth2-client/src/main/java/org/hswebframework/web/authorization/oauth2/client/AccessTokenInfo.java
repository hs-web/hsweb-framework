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
package org.hswebframework.web.authorization.oauth2.client;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.*;

import java.io.Serializable;

/**
 * 默认的服务实现
 *
 * @author zhouhao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenInfo implements Serializable {
    private static final long serialVersionUID = -6261971233479574076L;
    private String id;
    //授权码
    @JSONField(name = "access_token")
    private String accessToken;
    //更新码
    @JSONField(name = "refresh_token")
    private String refreshToken;
    //有效期
    @JSONField(name = "expires_in")
    private Integer expiresIn;
    //授权范围
    private String scope;

    private Long createTime;

    private Long updateTime;

    @JSONField(name = "token_type")
    private String tokenType;

    private String grantType;

    private String serverId;

    public boolean isExpire() {

        if (expiresIn == null) {
            return true;
        }
        if (expiresIn <= 0) {
            return false;
        }
        long time = updateTime == null ? createTime : updateTime;

        return System.currentTimeMillis() - time > expiresIn * 1000;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

}
