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

package org.hswebframework.web.service.oauth2.client.simple.request;

import org.hswebframework.expands.request.http.Response;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.authorization.oauth2.client.response.ResponseConvert;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.service.oauth2.client.request.ResponseConvertHandler;
import org.hswebframework.web.service.oauth2.client.request.ResponseJudge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

/**
 * @author zhouhao
 */
public class SimpleOAuth2Response implements OAuth2Response {

    private ResponseConvertHandler convertHandler;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ErrorType errorType;

    private byte[] data;

    private int status;

    private OAuth2Response proxy = this;

    public void judgeExpired(Supplier<OAuth2Response> expiredCallBack) {
        if (errorType == ErrorType.EXPIRED_TOKEN) {
            //尝试执行认证过时回调进行重试,并返回重试的结果
            OAuth2Response retryRes = expiredCallBack.get();
            if (retryRes == null) return;
            proxy = retryRes;
            proxy.onError((retryResponse, type) -> {
                if (type == ErrorType.EXPIRED_TOKEN) {
                    //重试后依然是认证过时,可能是错误类型判断错误或者服务端的问题?
                    logger.warn("still error [expired_token], maybe judge error or auth server error！ ");
                } else {
                    errorType = type;
                }
            });
            data = UnCheck.unCheck(proxy::asBytes);
            status = proxy.status();
        }
    }

    public SimpleOAuth2Response(Response response,
                                ResponseConvertHandler convertHandler,
                                ResponseJudge responseJudge) {
        this.convertHandler = convertHandler;
        data = UnCheck.unCheck(response::asBytes);
        status = response.getCode();
        errorType = responseJudge.judge(this);
    }

    @Override
    public String asString() {
        if (asBytes() == null) return null;
        return new String(asBytes());
    }

    @Override
    public byte[] asBytes() {
        return data;
    }

    @Override
    public <T> T as(ResponseConvert<T> convert) {
        return convert.convert(this);
    }

    @Override
    public <T> T as(Class<T> type) {
        return convertHandler.convert(this, type);
    }

    @Override
    public <T> List<T> asList(Class<T> type) {
        return convertHandler.convertList(this, type);
    }

    @Override
    public int status() {
        return status;
    }

    @Override
    public OAuth2Response onError(BiConsumer<OAuth2Response, ErrorType> onError) {
        if (null != errorType) {
            onError.accept(proxy, errorType);
        }
        return proxy;
    }

}
