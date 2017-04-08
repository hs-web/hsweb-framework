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

package org.hswebframework.web.oauth2.service;

import org.hswebframework.web.AuthorizeForbiddenException;
import org.hswebframework.web.NotFoundException;
import org.hswebframework.web.authorization.oauth2.api.OAuth2ServerService;
import org.hswebframework.web.authorization.oauth2.api.entity.AuthorizationCodeEntity;
import org.hswebframework.web.authorization.oauth2.api.entity.OAuth2AccessEntity;
import org.hswebframework.web.authorization.oauth2.api.entity.OAuth2ClientEntity;
import org.hswebframework.web.dao.oauth2.AuthorizationCodeDao;
import org.hswebframework.web.dao.oauth2.OAuth2AccessDao;
import org.hswebframework.web.dao.oauth2.OAuth2ClientDao;
import org.hswebframework.web.commons.entity.GenericEntity;
import org.hswebframework.web.commons.entity.factory.EntityFactory;
import org.hswebframework.web.entity.authorization.UserEntity;
import org.hswebframework.web.id.IDGenerator;
import org.hswebframework.web.service.DefaultDSLQueryService;
import org.hswebframework.web.service.authorization.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import static org.hswebframework.web.service.DefaultDSLDeleteService.createDelete;
import static org.hswebframework.web.service.DefaultDSLQueryService.createQuery;
import static org.hswebframework.web.service.DefaultDSLUpdateService.createUpdate;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
@Service("oAuth2ServerService")
@Transactional(rollbackFor = Throwable.class)
public class SimpleOAuth2ServerService implements OAuth2ServerService {

    private static final String cacheName = "hsweb.oauth2";
    @Resource
    private OAuth2ClientDao      oAuth2ClientDao;
    @Resource
    private OAuth2AccessDao      oAuth2AccessDao;
    @Resource
    private AuthorizationCodeDao authorizationCodeDao;
    @Resource
    private EntityFactory        entityFactory;

    @Resource
    private UserService userService;

    @Override
    public OAuth2ClientEntity getClient(String clientId) {
        return createQuery(oAuth2ClientDao)
                .where(GenericEntity.id, clientId)
                .single();
    }

    @Override
    public OAuth2ClientEntity getClient(String clientId, String clientSecret) {
        return createQuery(oAuth2ClientDao)
                .where(GenericEntity.id, clientId)
                // TODO: 17-2-28 key (clientSecret) 应该为常量
                .where("clientSecret", clientSecret)
                .single();
    }

    @Override
    public String requestCode(String clientId, String userId, String scope, String redirectUri) {
        String code = IDGenerator.MD5.generate();
        //删除旧的code
        createDelete(authorizationCodeDao)
                // TODO: 17-2-28 key  应该为常量
                .where("userId", userId)
                .and("clientId", userId)
                .exec();
        AuthorizationCodeEntity codeEntity = entityFactory.newInstance(AuthorizationCodeEntity.class);
        codeEntity.setCreateTime(System.currentTimeMillis());
        codeEntity.setClientId(clientId);
        codeEntity.setUserId(userId);
        codeEntity.setCode(code);
        codeEntity.setScope(scope);
        codeEntity.setRedirectUri(redirectUri);
        authorizationCodeDao.insert(codeEntity);
        return code;
    }

    protected OAuth2AccessEntity createNewAccess() {
        OAuth2AccessEntity entity = entityFactory.newInstance(OAuth2AccessEntity.class);
        entity.setCreateTime(System.currentTimeMillis());
        entity.setAccessToken(IDGenerator.MD5.generate());
        entity.setRefreshToken(IDGenerator.MD5.generate());
        return entity;
    }

    @Override
    public OAuth2AccessEntity requestTokenByCode(String code,
                                                 String clientId,
                                                 String clientSecret,
                                                 String scope,
                                                 String redirectUri) {
        AuthorizationCodeEntity codeEntity =
                createQuery(authorizationCodeDao)
                        .where("code", code)
                        .and("clientId", clientId)
                        .single();
        if (codeEntity == null) {
            throw new NotFoundException("code not found!");
        }
        try {
//            if (!redirectUri.equals(codeEntity.getRedirectUri())) {
//                // redirectUri error
//                throw new IllegalArgumentException("redirectUri error!");
//            }
            //授权码已经创建超时(10分钟)
            if (System.currentTimeMillis() - codeEntity.getCreateTime() > 10 * 60 * 1000) {
                throw new NotFoundException("time out!");
            }
            // TODO: 17-2-28  验证scope

            OAuth2ClientEntity clientEntity = getClient(clientId, clientSecret);
            if (null == clientEntity) {
                // TODO: 17-2-28 自定义异常
                throw new IllegalArgumentException("client not found!");
            }
            if (!clientEntity.grantTypeIsSupport("authorization_code")) {
                throw new UnsupportedOperationException("grant_type:authorization_code not support!");
            }
            OAuth2AccessEntity accessEntity = createNewAccess();
            accessEntity.setUserId(codeEntity.getUserId());
            accessEntity.setClientId(clientId);
            // TODO: 17-2-28 过期时间应该可配置
            accessEntity.setExpiresIn(3600L);
            accessEntity.setScope(scope);
            oAuth2AccessDao.insert(accessEntity);
            return accessEntity;
        } finally {
            //删除使用过的授权码
            createDelete(authorizationCodeDao)
                    .where("code", code)
                    .and("clientId", clientId)
                    .exec();
        }
    }

    @Override
    public OAuth2AccessEntity requestTokenByClientCredential(String clientId, String clientSecret) {
        OAuth2ClientEntity clientEntity = getClient(clientId, clientSecret);
        if (null == clientEntity) {
            // TODO: 17-2-28 自定义异常
            throw new NotFoundException("client not found!");
        }
        if (!clientEntity.grantTypeIsSupport("client_credential")) {
            throw new UnsupportedOperationException("grant_type:client_credential not support!");
        }
        return createNewTokenAndRemoveOld(clientEntity);
    }

    protected OAuth2AccessEntity createNewTokenAndRemoveOld(OAuth2ClientEntity clientEntity) {
        OAuth2AccessEntity oldEntity = DefaultDSLQueryService
                .createQuery(oAuth2AccessDao)
                .where("clientId", clientEntity.getId())
                .and("userId", clientEntity.getOwnerId())
                .single();
        OAuth2AccessEntity newEntity = createNewAccess();
        if (oldEntity != null) {
            newEntity.setScope(oldEntity.getScope());
            newEntity.setExpiresIn(oldEntity.getExpiresIn());
            newEntity.setRefreshToken(oldEntity.getRefreshToken());
        } else {
            newEntity.setExpiresIn(3600L);
            newEntity.setScope("public");
        }
        newEntity.setClientId(clientEntity.getId());
        newEntity.setUserId(clientEntity.getOwnerId());
        oAuth2AccessDao.insert(newEntity);
        return newEntity;
    }

    @Override
    public OAuth2AccessEntity requestTokenByImplicit(String clientId, String scope) {
        OAuth2ClientEntity clientEntity = getClient(clientId);
        if (null == clientEntity) {
            // TODO: 17-2-28 自定义异常
            throw new NotFoundException("client not found!");
        }
        if (!clientEntity.grantTypeIsSupport("implicit")) {
            throw new UnsupportedOperationException("grant_type:implicit not support!");
        }
        return createNewTokenAndRemoveOld(clientEntity);
    }

    @Override
    public OAuth2AccessEntity requestTokenByPassword(String username, String password) {
        UserEntity entity = userService.selectByUsername(username);
        if (null == entity) throw new NotFoundException("user not found");
        if (!userService.encodePassword(password, entity.getSalt()).equals(entity.getPassword()))
            throw new AuthorizeForbiddenException("password error");
        OAuth2ClientEntity clientEntity = DefaultDSLQueryService.createQuery(oAuth2ClientDao).where("user_id", entity.getId()).single();
        if (clientEntity == null) {
            throw new NotFoundException("client not found");
        }
        if (!clientEntity.grantTypeIsSupport("password")) {
            throw new UnsupportedOperationException("grant_type:password not support!");
        }
        OAuth2AccessEntity accessEntity = createNewAccess();
        accessEntity.setUserId(entity.getId());
        accessEntity.setScope("public");
        accessEntity.setExpiresIn(3600L);
        accessEntity.setClientId(clientEntity.getId());
        oAuth2AccessDao.insert(accessEntity);
        return accessEntity;
    }

    @Override
    public OAuth2AccessEntity refreshToken(String clientId, String clientSecret, String refreshToken, String scope) {
        OAuth2ClientEntity clientEntity = getClient(clientId, clientSecret);
        if (null == clientEntity) {
            // TODO: 17-2-28 自定义异常
            throw new NotFoundException("client not found!");
        }
        if (!clientEntity.grantTypeIsSupport("refresh_token")) {
            throw new UnsupportedOperationException("grant_type:refresh_token not support!");
        }
        OAuth2AccessEntity accessEntity = DefaultDSLQueryService.createQuery(oAuth2AccessDao)
                .where("refreshToken", refreshToken)
                .and("clientId", clientId)
                .single();
        if (null == accessEntity) {
            throw new NotFoundException("access not found!");
        }
        //30天过期
        long refreshTokenTimeOut = 30 * 24 * 60 * 60 * 1000L;
        if (System.currentTimeMillis() - accessEntity.getCreateTime() > refreshTokenTimeOut) {
            throw new NotFoundException("refresh_token time out");
        }
        accessEntity.setAccessToken(IDGenerator.MD5.generate());
        accessEntity.setUpdateTime(System.currentTimeMillis());
        accessEntity.setScope(scope);
        createUpdate(oAuth2AccessDao, accessEntity)
                .includes("accessToken", "updateTime", "scope")
                .where("refreshToken", refreshToken)
                .and("clientId", clientId)
                .exec();
        return accessEntity;
    }

    @Override
    public OAuth2AccessEntity getAccessToken(String accessToken) {
        return DefaultDSLQueryService.createQuery(oAuth2AccessDao).where("accessToken", accessToken).single();
    }
}
