package org.hswebframework.web.example.oauth2.github;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.WebUtil;
import org.hswebframework.web.authorization.oauth2.client.exception.OAuth2RequestException;
import org.hswebframework.web.authorization.oauth2.client.request.definition.ResponseJudgeForProviderDefinition;
import org.hswebframework.web.authorization.oauth2.client.response.OAuth2Response;
import org.hswebframework.web.oauth2.core.ErrorType;

import java.util.Map;

public class GithubResponseJudge implements ResponseJudgeForProviderDefinition {
    @Override
    public String getProvider() {
        return "github";
    }

    @Override
    @SuppressWarnings("all")
    public ErrorType judge(OAuth2Response response) {

        String res= response.asString();
        Map<String,Object> responseMap ;
        if(res.startsWith("{")){
            responseMap= JSON.parseObject(res);
        }else{
            responseMap= (Map) WebUtil.queryStringToMap(res,"utf-8");
        }
        if(response.status()==401){
            throw new OAuth2RequestException(String.valueOf(responseMap.get("message")),ErrorType.UNAUTHORIZED_CLIENT,response);
        }
        if(responseMap.get("error")!=null){
            throw new OAuth2RequestException(String.valueOf(responseMap.get("error_description")),ErrorType.EXPIRED_CODE,response);

        }
        return null;
    }
}
