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
 */
package org.hswebframework.web.service.oauth2.client.simple;

import org.hswebframework.web.dao.oauth2.client.OAuth2ServerConfigDao;
import org.hswebframework.web.entity.oauth2.client.OAuth2ServerConfigEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.GenericEntityService;
import org.hswebframework.web.service.oauth2.client.OAuth2ServerConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 默认的服务实现
 *
 * @author hsweb-generator-online
 */
@Service("oAuth2ServerConfigService")
public class SimpleOAuth2ServerConfigService extends GenericEntityService<OAuth2ServerConfigEntity, String>
        implements OAuth2ServerConfigService {
    @Autowired
    private OAuth2ServerConfigDao oAuth2ServerConfigDao;
   @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public OAuth2ServerConfigDao getDao() {
        return oAuth2ServerConfigDao;
    }

}
