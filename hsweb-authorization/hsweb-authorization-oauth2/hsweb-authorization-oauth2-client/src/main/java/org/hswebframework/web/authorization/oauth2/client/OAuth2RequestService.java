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

import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Event;
import org.hswebframework.web.authorization.oauth2.client.listener.OAuth2Listener;

/**
 * OAuth2请求服务接口,用于创建OAuth2请求,注册监听器等操作
 *
 * @author zhouhao
 * @@since 3.0
 */
public interface OAuth2RequestService {

    /**
     * 创建一个OAuth2服务的会话创建器
     *
     * @param serverId 服务ID,serverId是由接口的实现模块自行定义的
     * @return OAuth2会话创建器
     * @see OAuth2SessionBuilder
     */
    OAuth2SessionBuilder create(String serverId);

    /**
     * 注册一个监听器到指定的OAuth2服务
     *
     * @param serverId 服务ID
     * @param listener 监听器
     */
    void registerListener(String serverId, OAuth2Listener<? extends OAuth2Event> listener);

    /**
     * 触发一个监听事件
     *
     * @param serverId 服务ID
     * @param event    事件实例
     */
    void doEvent(String serverId, OAuth2Event event);

    /**
     * 触发一个指定类型的事件
     * @param serverId
     * @param event
     * @param eventType
     */
    void doEvent(String serverId, OAuth2Event event, Class<? extends OAuth2Event> eventType);
}
