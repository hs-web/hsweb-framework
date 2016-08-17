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

package org.hsweb.web.oauth2.controller;

import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.controller.GenericController;
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.core.utils.WebUtil;
import org.hsweb.web.oauth2.po.OAuth2Client;
import org.hsweb.web.oauth2.service.OAuth2ClientService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/oauth2/client")
@AccessLogger("OAuth2客户端")
@Authorize(module = "oauth2-manager")
public class OAuth2ClientController extends GenericController<OAuth2Client, String> {
    @Resource
    private OAuth2ClientService oAuth2ClientService;

    @Override
    protected OAuth2ClientService getService() {
        return oAuth2ClientService;
    }

    @RequestMapping(value = "/enable/{id}", method = RequestMethod.PUT)
    @AccessLogger("启用")
    @Authorize(action = "enable")
    protected ResponseMessage enable(@PathVariable("id") String id) {
        oAuth2ClientService.enable(id);
        return ResponseMessage.ok();
    }


    @RequestMapping(value = "/disable/{id}", method = RequestMethod.PUT)
    @AccessLogger("禁用")
    @Authorize(action = "disable")
    protected ResponseMessage disbale(@PathVariable("id") String id) {
        oAuth2ClientService.disable(id);
        return ResponseMessage.ok();
    }


    @RequestMapping(value = "/secret/{id}", method = RequestMethod.PUT)
    @AccessLogger("刷新密钥")
    @Authorize(action = "U")
    protected ResponseMessage refreshSecret(@PathVariable("id") String id) {
        return ResponseMessage.ok(oAuth2ClientService.refreshSecret(id));
    }

    @RequestMapping(value = "/secret", method = RequestMethod.PUT)
    @AccessLogger("刷新当前用户密钥")
    @Authorize
    protected ResponseMessage refreshLoginUserSecret() {
        User user = WebUtil.getLoginUser();
        OAuth2Client client = oAuth2ClientService.selectSingle(QueryParam.build().where("userId", user.getId()));
        if (client == null) {
            throw new NotFoundException("未绑定客户端");
        }
        return ResponseMessage.ok(oAuth2ClientService.refreshSecret(client.getId()));
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    @AccessLogger("获取当前用户持有的客户端信息")
    @Authorize
    protected ResponseMessage loginUserClient() {
        User user = WebUtil.getLoginUser();
        OAuth2Client client = oAuth2ClientService.selectSingle(QueryParam.build().where("userId", user.getId()));
        if (client == null) {
            throw new NotFoundException("未绑定客户端");
        }
        return ResponseMessage.ok(client);
    }

}
