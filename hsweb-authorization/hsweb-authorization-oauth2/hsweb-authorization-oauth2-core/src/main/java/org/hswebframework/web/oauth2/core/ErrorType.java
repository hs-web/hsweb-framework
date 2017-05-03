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

package org.hswebframework.web.oauth2.core;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public enum ErrorType {
    ILLEGAL_CODE(1001), //错误的授权码
    ILLEGAL_ACCESS_TOKEN(1002), //错误的access_token
    ILLEGAL_CLIENT_ID(1003),//客户端信息错误
    ILLEGAL_CLIENT_SECRET(1004),//客户端密钥错误
    ILLEGAL_GRANT_TYPE(1005), //错误的授权方式
    ILLEGAL_RESPONSE_TYPE(1006),//response_type 错误
    ILLEGAL_AUTHORIZATION(1007),//Authorization 错误
    ILLEGAL_REFRESH_TOKEN(1008),//refresh_token 错误
    ILLEGAL_REDIRECT_URI(1009), //redirect_url 错误
    ILLEGAL_SCOPE(1010), //scope 错误
    ILLEGAL_USERNAME(1011), //username 错误
    ILLEGAL_PASSWORD(1012), //password 错误

    SCOPE_OUT_OF_RANGE(2010), //scope超出范围

    UNAUTHORIZED_CLIENT(4010), //无权限
    EXPIRED_TOKEN(4011), //TOKEN过期
    INVALID_TOKEN(4012), //TOKEN已失效
    UNSUPPORTED_GRANT_TYPE(4013), //不支持的认证类型
    UNSUPPORTED_RESPONSE_TYPE(4014), //不支持的响应类型

    EXPIRED_CODE(4015), //AUTHORIZATION_CODE过期
    EXPIRED_REFRESH_TOKEN(4020), //AUTHORIZATION_CODE过期

    CLIENT_DISABLED(4016),//客户端已被禁用

    CLIENT_NOT_EXIST(4040),//客户端不存在

    USER_NOT_EXIST(4041),//客户端不存在

    ACCESS_DENIED(503), //访问被拒绝
    OTHER(5001); //其他错误 ;

    private final String message;
    private final int    code;
    static final Map<Integer, ErrorType> codeMapping = Arrays.stream(ErrorType.values())
            .collect(Collectors.toMap(ErrorType::code, type -> type));

    ErrorType(int code) {
        this.code = code;
        message = this.name().toLowerCase();
    }

    ErrorType(int code, String message) {
        this.message = message;
        this.code = code;
    }

    public String message() {
        if (message == null) return this.name();
        return message;
    }

    public int code() {
        return code;
    }

    public <T> T throwThis(Function<ErrorType, ? extends RuntimeException> errorTypeFunction) {
        throw errorTypeFunction.apply(this);
    }

    public <T> T throwThis(BiFunction<ErrorType, String, ? extends RuntimeException> errorTypeFunction, String message) {
        throw errorTypeFunction.apply(this, message);
    }

    public static Optional<ErrorType> fromCode(int code) {
        return Optional.ofNullable(codeMapping.get(code));
    }

}
