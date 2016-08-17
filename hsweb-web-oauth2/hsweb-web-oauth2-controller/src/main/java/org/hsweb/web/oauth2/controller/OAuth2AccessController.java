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
import org.hsweb.web.core.authorize.annotation.Authorize;
import org.hsweb.web.core.logger.annotation.AccessLogger;
import org.hsweb.web.core.message.ResponseMessage;
import org.hsweb.web.oauth2.po.OAuth2Access;
import org.hsweb.web.oauth2.service.OAuth2ClientService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/oauth2/access")
@AccessLogger("OAuth2授权码管理")
@Authorize(module = "oauth2-access")
public class OAuth2AccessController {
    @Resource
    private OAuth2ClientService oAuth2ClientService;

    @RequestMapping(method = RequestMethod.GET)
    @Authorize(action = "R")
    @AccessLogger("授权列表")
    public ResponseMessage accessList(QueryParam param) {
        return ResponseMessage.ok(oAuth2ClientService.selectAccessList(param))
                .exclude(OAuth2Access.class, "accessToken")
                .onlyData();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @Authorize(action = "D")
    @AccessLogger("删除授权")
    public ResponseMessage deleteAccess(@PathVariable("id") String id) {
        return ResponseMessage.ok(oAuth2ClientService.deleteAccess(id));
    }

}
