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

package org.hswebframework.web.service.oauth2.server.simple;

import org.hswebframework.web.entity.oauth2.server.AuthorizationCodeEntity;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCode;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeRequest;
import org.hswebframework.web.authorization.oauth2.server.support.code.AuthorizationCodeService;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.dao.oauth2.server.AuthorizationCodeDao;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLDeleteService;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleAuthorizationCodeService implements AuthorizationCodeService {
    private AuthorizationCodeDao authorizationCodeDao;
    private EntityFactory        entityFactory;
    private CodeGenerator codeGenerator = IDGenerator.MD5::generate;

    public SimpleAuthorizationCodeService(AuthorizationCodeDao authorizationCodeDao, EntityFactory entityFactory) {
        this.authorizationCodeDao = authorizationCodeDao;
        this.entityFactory = entityFactory;
    }

    public SimpleAuthorizationCodeService setCodeGenerator(CodeGenerator codeGenerator) {
        if (codeGenerator != null) {
            this.codeGenerator = codeGenerator;
        }
        return this;
    }

    @Override
    public String createAuthorizationCode(AuthorizationCodeRequest request) {
        AuthorizationCodeEntity codeEntity = entityFactory.newInstance(AuthorizationCodeEntity.class);
        codeEntity.setClientId(request.getClientId());
        codeEntity.setRedirectUri(request.getRedirectUri());
        codeEntity.setCreateTime(System.currentTimeMillis());
        codeEntity.setUserId(request.getUserId());
        codeEntity.setScope(request.getScope());
        codeEntity.setCode(codeGenerator.generate());
        authorizationCodeDao.insert(codeEntity);
        return codeEntity.getCode();
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AuthorizationCode consumeAuthorizationCode(String code) {
        AuthorizationCodeEntity codeEntity = DefaultDSLQueryService
                .createQuery(authorizationCodeDao)
                .where("code", code).single();
        //delete
        DefaultDSLDeleteService.createDelete(authorizationCodeDao)
                .where("code", code).exec();
        return codeEntity;
    }
}
