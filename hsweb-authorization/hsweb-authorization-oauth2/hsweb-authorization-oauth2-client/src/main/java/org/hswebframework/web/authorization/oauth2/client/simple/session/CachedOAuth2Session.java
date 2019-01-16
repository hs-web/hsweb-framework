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

package org.hswebframework.web.authorization.oauth2.client.simple.session;

import org.hswebframework.web.authorization.oauth2.client.AccessTokenInfo;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;

/**
 *
 * @author zhouhao
 */
public class CachedOAuth2Session extends DefaultOAuth2Session {


    public CachedOAuth2Session(AccessTokenInfo tokenInfo) {
        super.accessTokenInfo = tokenInfo;
    }

    @Override
    public OAuth2Session authorize() {
        // do no thing
        return this;
    }
}
