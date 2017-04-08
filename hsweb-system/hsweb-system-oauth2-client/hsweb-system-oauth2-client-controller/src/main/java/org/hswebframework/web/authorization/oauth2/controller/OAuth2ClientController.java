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

package org.hswebframework.web.authorization.oauth2.controller;

import org.hswebframework.web.BusinessException;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2CodeAuthBeforeEvent;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author zhouhao
 */
@Controller
@RequestMapping("${hsweb.web.mappings.oauth2-client-callback:oauth2}")
public class OAuth2ClientController {

    private OAuth2RequestService oAuth2RequestService;

    @Autowired
    public void setoAuth2RequestService(OAuth2RequestService oAuth2RequestService) {
        this.oAuth2RequestService = oAuth2RequestService;
    }

    private static final String STATE_SESSION_KEY = "OAUTH2_STATE";

    @GetMapping("/state")
    @ResponseBody
    public ResponseMessage<String> requestState(HttpSession session) {
        String state = IDGenerator.RANDOM.generate();
        session.setAttribute(STATE_SESSION_KEY, state);
        return ResponseMessage.ok(state);
    }

    @GetMapping("/callback/{serverId}")
    public RedirectView callback(@RequestParam(defaultValue = "/") String redirect,
                                 @PathVariable String serverId,
                                 @RequestParam String code,
                                 @RequestParam String state,
                                 HttpServletRequest request,
                                 HttpSession session) {
        try {
            String cachedState = (String) session.getAttribute(STATE_SESSION_KEY);
            if (!state.equals(cachedState)) throw new BusinessException("state error");

            oAuth2RequestService.doEvent(serverId, new OAuth2CodeAuthBeforeEvent(code, state, request::getParameter));
            // TODO: 17-4-7 验证并解码redirect
            return new RedirectView(redirect);
        } finally {
            session.removeAttribute(STATE_SESSION_KEY);
        }
    }
}
