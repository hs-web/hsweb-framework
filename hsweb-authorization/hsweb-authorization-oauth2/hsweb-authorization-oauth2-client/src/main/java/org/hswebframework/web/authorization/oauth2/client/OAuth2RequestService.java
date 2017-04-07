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

package org.hswebframework.web.authorization.oauth2.client;

import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Event;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Listener;
import org.hswebframework.web.authorization.oauth2.client.request.OAuth2Session;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;

import java.util.List;
import java.util.function.Function;

/**
 * @author zhouhao
 */
public interface OAuth2RequestService {
    OAuth2SessionBuilder create(String serverId);

    void registerListener(String serverId, OAuth2Listener<? extends OAuth2Event> listener);

    void doEvent(String serverId, OAuth2Event event);

    void doEvent(String serverId, OAuth2Event event, Class<? extends OAuth2Event> eventType);


    static void main(String[] args) {
        OAuth2RequestService requestService = null;
        Function<OAuth2Response, Authentication> authExchanger = null;
        String authorizationCode = "";

        OAuth2Session session = requestService
                .create("hsweb-user-center")
                .byAuthorizationCode(authorizationCode);

        Authentication authentication = session
                .request("oauth2/user-auth-info")
                .get()
                .as(Authentication.class);

        session.request("menu")
                .param("paging", "0")
                .get().as(List.class);

        authentication.getUser().getId();

    }
}
