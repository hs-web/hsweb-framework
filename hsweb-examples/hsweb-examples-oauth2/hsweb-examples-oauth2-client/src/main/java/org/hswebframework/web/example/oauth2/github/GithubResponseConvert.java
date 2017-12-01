package org.hswebframework.web.example.oauth2.github;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.oauth2.client.AccessTokenInfo;
import org.hswebframework.web.authorization.oauth2.client.request.definition.ResponseConvertForProviderDefinition;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;

import java.util.List;
import java.util.Map;

@Slf4j
public class GithubResponseConvert implements ResponseConvertForProviderDefinition {
    @Override
    public String getProvider() {
        return "github";
    }

    @Override
    public <T> T convert(OAuth2Response response, Class<T> type) {
        String result = response.asString();

        if (result.startsWith("{")) {
            return JSON.parseObject(result, type);
        }
        if (result.startsWith("[")) {
            throw new UnsupportedOperationException("response is json array,you should call convertList method !");
        }
        Map<String, String> responseMap = WebUtil.queryStringToMap(result, "utf-8");
        if (type == Map.class) {
            return ((T) responseMap);
        }
        if (AccessTokenInfo.class.isAssignableFrom(type)) {
            AccessTokenInfo info;
            if(type!=AccessTokenInfo.class) {
                try {
                    info = ((AccessTokenInfo) type.newInstance());
                } catch (Exception e) {
                    log.warn("can not new instance {} use default AccessTokenInfo", type, e);
                    info = new AccessTokenInfo();
                }
            }else{
                info = new AccessTokenInfo();
            }
            info.setAccessToken(responseMap.get("access_token"));
            info.setScope(responseMap.get("scope"));
            info.setTokenType(responseMap.get("token_type"));
            info.setExpiresIn(-1);
            return ((T) info);
        }
        return null;
    }

    @Override
    public <T> List<T> convertList(OAuth2Response response, Class<T> type) {
        String result = response.asString();

        if (result.startsWith("{")) {
            throw new UnsupportedOperationException("response is json array,you should call convertList method !");
        }
        if (result.startsWith("[")) {
            return JSON.parseArray(result, type);
        }
        throw new UnsupportedOperationException("response format is not support yet,you can call response.as(ResponseConvert) method!");

    }
}
