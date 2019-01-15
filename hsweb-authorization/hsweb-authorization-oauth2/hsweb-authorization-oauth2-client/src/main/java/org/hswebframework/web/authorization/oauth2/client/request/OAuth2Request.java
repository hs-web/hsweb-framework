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

package org.hswebframework.web.authorization.oauth2.client.request;

import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;

/**
 * OAuth2请求接口,用于发起OAuth2请求
 *
 * @author zhouhao
 */
public interface OAuth2Request {

    OAuth2Request onRefreshTokenExpired(TokenExpiredCallBack refreshTokenExpiredCallBack);

    OAuth2Request onTokenExpired(TokenExpiredCallBack callback);

    /**
     * 设置请求参数,相当于/url?name=value
     *
     * @param name  参数名称
     * @param value 参数值
     * @return request自身
     */
    OAuth2Request param(String name, Object value);

    OAuth2Request params(Map<String, String> params);

    OAuth2Response upload(String name, InputStream inputStream);

    OAuth2Response upload(String name, InputStream inputStream,String fileName);

    /**
     * 设置请求体,将内容根据contentType(默认application/json)序列化为对应的请求数据
     *
     * @param value 请求内容
     * @return request自身
     */
    OAuth2Request requestBody(String value);

    /**
     * 设置请求头
     *
     * @param name  名称
     * @param value 值
     * @return request自身
     */
    OAuth2Request header(String name, String value);

    /**
     * 设置cookie
     *
     * @param cookie 值
     * @return request自身
     */
    OAuth2Request cookie(String cookie);

    /**
     * 设置请求的contentType
     *
     * @param contentType
     * @return request自身
     * @see "application/json"
     */
    OAuth2Request contentType(String contentType);

    /**
     * 设置接受响应的格式,相当与请求头:Accept
     *
     * @param accept
     * @return request自身
     * @see "application/json"
     */
    OAuth2Request accept(String accept);

    /**
     * 设置请求超时时间,超时后回调 timeoutConsumer
     *
     * @param millisecond     超时时间（毫秒）,小于0则不设置超时
     * @param timeoutCallBack 超时后的处理回调
     * @return request自身
     * @see Consumer
     */
    OAuth2Request timeout(long millisecond, Consumer<OAuth2Request> timeoutCallBack);

    /**
     * 以GET方式请求,并返回请求结果
     *
     * @return 请求结果
     */
    OAuth2Response get();

    /**
     * 以PUT方式请求,并返回请求结果
     *
     * @return 请求结果
     */
    OAuth2Response put();

    /**
     * 以POST方式请求,并返回请求结果
     *
     * @return 请求结果
     */
    OAuth2Response post();

    /**
     * 以DELETE方式请求,并返回请求结果
     *
     * @return 请求结果
     */
    OAuth2Response delete();

    /**
     * 以PATCH方式请求,并返回请求结果
     *
     * @return 请求结果
     */
    OAuth2Response patch();

}
