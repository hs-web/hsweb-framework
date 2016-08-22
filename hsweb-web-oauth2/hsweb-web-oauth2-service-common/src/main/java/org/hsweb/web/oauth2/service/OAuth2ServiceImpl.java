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

import org.apache.commons.beanutils.BeanUtilsBean;
import org.hsweb.web.bean.common.DeleteParam;
import org.hsweb.web.bean.common.InsertParam;
import org.hsweb.web.bean.common.QueryParam;
import org.hsweb.web.bean.common.UpdateParam;
import org.hsweb.web.bean.po.user.User;
import org.hsweb.web.core.exception.AuthorizeException;
import org.hsweb.web.core.exception.BusinessException;
import org.hsweb.web.core.exception.NotFoundException;
import org.hsweb.web.oauth2.dao.OAuth2AccessMapper;
import org.hsweb.web.oauth2.exception.AccessTimeoutException;
import org.hsweb.web.oauth2.po.OAuth2Access;
import org.hsweb.web.service.config.ConfigService;
import org.hsweb.web.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service("oAuth2Service")
public class OAuth2ServiceImpl implements OAuth2Service {

    @Resource
    private OAuth2AccessMapper oAuth2AccessMapper;

    @Resource
    private UserService userService;

    @Resource
    private ConfigService configService;

    @Autowired(required = false)
    private CacheManager cacheManager;
    static final String cacheName = "hsweb.oauth2";

    @Override
    public void addAccessToken(OAuth2Access auth2Access) {
        if (auth2Access.getId() == null) {
            auth2Access.setId(OAuth2Access.createUID());
        }
        // TODO: 16-8-17  保存n分钟再删除
        //删除旧的token
        List<OAuth2Access> accesses = oAuth2AccessMapper.select(QueryParam.build().where("userId", auth2Access.getUserId()));
        if (accesses.size() > 0) {
            accesses.forEach(this::removeAccessFromCache);
            oAuth2AccessMapper.delete(DeleteParam.build().where("userId", auth2Access.getUserId()));
        }
        oAuth2AccessMapper.insert(InsertParam.build(auth2Access));
    }

    private void removeAccessFromCache(OAuth2Access auth2Access) {
        //移除旧的缓存
        if (cacheManager != null) {
            String cacheKey = "accessToken:".concat(auth2Access.getAccessToken());
            Cache cache = cacheManager.getCache(cacheName);
            cache.evict(cacheKey);
        }
    }

    @Override
    public void refreshToken(OAuth2Access auth2Access) {
        auth2Access.setCreateDate(new Date());
        OAuth2Access old = oAuth2AccessMapper.selectByRefreshToken(auth2Access.getRefreshToken());
        if (old == null) {
            throw new NotFoundException("refreshToken不存在");
        }
        //修改
        oAuth2AccessMapper.update(UpdateParam.build(auth2Access)
                .includes("accessToken", "expireIn", "createDate")
                .where("refreshToken", auth2Access.getRefreshToken()));
        //移除旧的缓存
        removeAccessFromCache(old);
    }

    @Override
    @Transactional(noRollbackFor = AccessTimeoutException.class)
    public User getUserByAccessToken(String accessToken) {
        OAuth2Access auth2Access = null;
        Cache cache = null;
        String cacheKey = "accessToken:".concat(accessToken);
        boolean inCache = false;
        if (cacheManager != null) {
            cache = cacheManager.getCache(cacheName);
            if (cache != null) {
                Cache.ValueWrapper wrapper = cache.get(cacheKey);
                if (wrapper != null) {
                    auth2Access = (OAuth2Access) wrapper.get();
                    inCache = true;
                }
            }
        }
        if (auth2Access == null)
            auth2Access = oAuth2AccessMapper.selectByAccessToken(accessToken);
        if (auth2Access == null) {
            return null;
        }
        //判断是否已超时
        if (auth2Access.getLeftTime() <= 0) {
            if (cache != null) {
                cache.evict(cacheKey);
            }
            // TODO: 16-8-17 token删除还是刷新时更新？
            oAuth2AccessMapper.deleteById(auth2Access.getId());
            throw new AuthorizeException("expired_token");
        }
        if (!inCache) {
            User user = userService.selectByPk(auth2Access.getUserId());
            user.initRoleInfo();
            user.setPassword(null);
            User newUser = new User();
            try {
                BeanUtilsBean.getInstance().getPropertyUtils()
                        .copyProperties(newUser, user);
            } catch (Exception e) {
            }
            auth2Access.setUser(newUser);
            cache.put(cacheKey, auth2Access);
            return newUser;
        } else {
            return auth2Access.getUser();
        }
    }

    @Override
    public int getDefaultExpireIn() {
        return configService.getInt("oauth2", "expire_in", 3600);
    }

}
