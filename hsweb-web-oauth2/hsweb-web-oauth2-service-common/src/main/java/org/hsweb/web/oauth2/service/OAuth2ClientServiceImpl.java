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

package org.hsweb.web.oauth2.service;

import org.hsweb.commons.MD5;
import org.hsweb.web.bean.common.PagerResult;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateMapParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.core.utils.RandomUtil;
import org.hsweb.web.oauth2.dao.OAuth2AccessMapper;
import org.hsweb.web.oauth2.dao.OAuth2ClientMapper;
import org.hsweb.web.oauth2.po.OAuth2Access;
import org.hsweb.web.oauth2.po.OAuth2Client;
import org.hsweb.web.service.impl.AbstractServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.UUID;

@Service("oAuth2ClientService")
public class OAuth2ClientServiceImpl extends AbstractServiceImpl<OAuth2Client, String> implements OAuth2ClientService {

    @Resource
    private OAuth2ClientMapper oAuth2ClientMapper;

    @Resource
    private OAuth2AccessMapper oAuth2AccessMapper;

    @Override
    protected OAuth2ClientMapper getMapper() {
        return oAuth2ClientMapper;
    }

    @Override
    public PagerResult<OAuth2Access> selectAccessList(QueryParam param) {
        PagerResult<OAuth2Access> pagerResult = new PagerResult<>();
        param.setPaging(false);
        int total = getMapper().total(param);
        pagerResult.setTotal(total);
        param.rePaging(total);
        pagerResult.setData(oAuth2AccessMapper.select(param));
        return pagerResult;
    }

    @Override
    public int deleteAccess(String accessId) {
        return oAuth2AccessMapper.deleteById(accessId);
    }

    @Override
    public String insert(OAuth2Client data) {
        data.setSecret(MD5.encode(UUID.randomUUID().toString() + Math.random()));
        data.setStatus(1);
        return super.insert(data);
    }

    @Override
    public String refreshSecret(String clientId) {
        String secret = MD5.encode(UUID.randomUUID().toString() + Math.random());
        int size = oAuth2ClientMapper.update((UpdateParam) UpdateMapParam.build().set("secret", secret).where("id", clientId));
        if (size != 1) throw new NotFoundException("客户端不存在");
        return secret;
    }

    @Override
    public void enable(String id) {
        OAuth2Client old = selectByPk(id);
        assertNotNull(old, "客户端不存在");
        oAuth2ClientMapper.update((UpdateParam) UpdateMapParam.build().set("status", 1).where("id", id));
    }

    @Override
    public void disable(String id) {
        OAuth2Client old = selectByPk(id);
        assertNotNull(old, "客户端不存在");
        oAuth2ClientMapper.update((UpdateParam) UpdateMapParam.build().set("status", -1).where("id", id));
    }

    @Override
    public int update(OAuth2Client data) {
        return getMapper().update(UpdateParam.build(data).excludes("secret","status").where("id", data.getId()));
    }
}
