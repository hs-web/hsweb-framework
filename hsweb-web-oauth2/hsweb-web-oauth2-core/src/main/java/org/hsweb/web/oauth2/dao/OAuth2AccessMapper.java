/*
 * Copyright 2015-2016 http://hsweb.me
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.hsweb.web.oauth2.dao;

import org.hsweb.web.dao.GenericMapper;
import org.hsweb.web.oauth2.po.OAuth2Access;

/**
 * OAuth2认证信息数据映射接口
 * Created by generator
 */
public interface OAuth2AccessMapper extends GenericMapper<OAuth2Access,String> {

    int deleteById(String id);

    OAuth2Access selectByAccessToken(String accessToken);

    OAuth2Access selectByRefreshToken(String refreshToken);
}
