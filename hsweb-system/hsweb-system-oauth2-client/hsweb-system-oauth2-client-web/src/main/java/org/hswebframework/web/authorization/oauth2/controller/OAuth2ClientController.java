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

package org.hswebframework.web.authorization.oauth2.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hswebframework.web.BusinessException;
import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.oauth2.client.OAuth2RequestService;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2CodeAuthBeforeEvent;
import org.hswebframework.web.controller.message.ResponseMessage;
import org.hswebframework.web.entity.oauth2.client.OAuth2ServerConfigEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.oauth2.core.OAuth2Constants;
import org.hswebframework.web.service.oauth2.client.OAuth2ServerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author zhouhao
 */
@Controller
@RequestMapping("${hsweb.web.mappings.oauth2-client-callback:oauth2}")
@Api(tags = "OAuth2.0-客户端-请求服务", value = "OAuth2.0客户端请求服务")
public class OAuth2ClientController {

    private OAuth2RequestService oAuth2RequestService;

    private OAuth2ServerConfigService oAuth2ServerConfigService;

    @Autowired
    public void setoAuth2ServerConfigService(OAuth2ServerConfigService oAuth2ServerConfigService) {
        this.oAuth2ServerConfigService = oAuth2ServerConfigService;
    }

    @Autowired
    public void setoAuth2RequestService(OAuth2RequestService oAuth2RequestService) {
        this.oAuth2RequestService = oAuth2RequestService;
    }

    private static final String STATE_SESSION_KEY = "OAUTH2_STATE";

    @GetMapping("/state")
    @ResponseBody
    @ApiOperation("申请一个state")
    public ResponseMessage<String> requestState(HttpSession session) {
        String state = IDGenerator.RANDOM.generate();
        session.setAttribute(STATE_SESSION_KEY, state);
        return ResponseMessage.ok(state);
    }

    @GetMapping("/boot/{serverId}")
    @ApiOperation("跳转至OAuth2.0服务授权页面")
    public RedirectView boot(@PathVariable String serverId,
                             @RequestParam(defaultValue = "/") String redirect,
                             HttpServletRequest request,
                             HttpSession session) throws UnsupportedEncodingException {
        OAuth2ServerConfigEntity entity = oAuth2ServerConfigService.selectByPk(serverId);
        if (entity == null) {
            return new RedirectView("/401.html");
        }
        String callback = WebUtil.getBasePath(request)
                .concat("oauth2/callback/")
                .concat(serverId).concat("/?redirect=")
                .concat(URLEncoder.encode(redirect, "UTF-8"));
        RedirectView view = new RedirectView(entity.getRealUrl(entity.getAuthUrl()));
        view.addStaticAttribute(OAuth2Constants.response_type, "code");
        view.addStaticAttribute(OAuth2Constants.state, requestState(session).getResult());
        view.addStaticAttribute(OAuth2Constants.client_id, entity.getClientId());
        view.addStaticAttribute(OAuth2Constants.redirect_uri, callback);
        return view;
    }

    @GetMapping("/callback/{serverId}")
    @ApiOperation(value = "OAuth2.0授权完成后回调", hidden = true)
    public RedirectView callback(@RequestParam(defaultValue = "/") String redirect,
                                 @PathVariable String serverId,
                                 @RequestParam String code,
                                 @RequestParam String state,
                                 HttpServletRequest request,
                                 HttpSession session) throws UnsupportedEncodingException {
        try {
            String cachedState = (String) session.getAttribute(STATE_SESSION_KEY);
            if (!state.equals(cachedState)) {
                throw new BusinessException(ErrorType.STATE_ERROR.name());
            }
            oAuth2RequestService.doEvent(serverId, new OAuth2CodeAuthBeforeEvent(code, state, request::getParameter));
            return new RedirectView(URLDecoder.decode(redirect, "UTF-8"));
        } finally {
            session.removeAttribute(STATE_SESSION_KEY);
        }
    }
}
