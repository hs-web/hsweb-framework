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

package org.hswebframework.web.authorization.oauth2.client.request;

import org.hswebframework.web.authorization.oauth2.client.AccessTokenInfo;

import java.io.Serializable;

/**
 * OAuth2会话,此会话保存的是 OAuth2授权成功后得到的access_token等相关信息.
 * 通过会话发起的OAuth2请求将自动带上access_token信息.
 *
 * @author zhouhao
 * @see OAuth2Request
 * @since 3.0
 */
public interface OAuth2Session{
    /**
     * 尝试进行认证
     *
     * @return 会话自身
     */
    OAuth2Session authorize();

    /**
     * 发起一个OAuth2请求,参数为接口地址
     *
     * @param uriOrUrl 请求地址,可以为URI或者URL
     * @return 请求接口
     */
    OAuth2Request request(String uriOrUrl);

    /**
     * 设置在请求OAuth2 授权的时候的参数(除了必要之外的参数),client_id,client_secret等信息不需要调用此方法设置
     *
     * @param name  参数名称
     * @param value 参数值
     * @return 会话自身
     */
    OAuth2Session param(String name, Object value);

    OAuth2Session scope(String scope);

    /**
     * 关闭会话,将清空
     */
    void close();

    /**
     * @return 是否已关闭
     */
    boolean isClosed();

    AccessTokenInfo requestAccessToken();

    AccessTokenInfo getAccessToken();

}
