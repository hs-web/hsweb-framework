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

package org.hswebframework.web.authorization.oauth2.client.response;

import org.hswebframework.web.authorization.oauth2.client.exception.OAuth2RequestException;
import org.hswebframework.web.oauth2.core.ErrorType;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * OAuth2 请求结果
 *
 * @author zhouhao
 */
public interface OAuth2Response {

    /**
     * @return 结果转为字符串
     */
    String asString();

    /**
     * @return 结果转为byte数组
     */
    byte[] asBytes();

    /**
     * 自定义转换方式
     *
     * @param convert 转换函数
     * @param <T>     转换结果类型
     * @return 转换结果
     */
    <T> T as(ResponseConvert<T> convert);

    /**
     * 转换为指定的类型
     *
     * @param type 类型Class
     * @param <T>  结果类型
     * @return 结果
     */
    <T> T as(Class<T> type);

    /**
     * 转换为指定类型的结果集
     *
     * @param type 类型Class
     * @param <T>  结果类型
     * @return 结果集合
     */
    <T> List<T> asList(Class<T> type);

    /**
     * @return 响应状态码
     */
    int status();

    /**
     * 判断是否成功,如果不成功,则抛出异常
     *
     * @return 响应结果本身
     */
    OAuth2Response onError(BiConsumer<OAuth2Response, ErrorType> onError);

    BiConsumer<OAuth2Response, ErrorType> throwOnError = (response, errorType) -> {
        throw new OAuth2RequestException(errorType, response);
    };
}
