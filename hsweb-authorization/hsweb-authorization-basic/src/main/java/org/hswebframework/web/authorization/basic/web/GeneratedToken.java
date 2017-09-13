package org.hswebframework.web.authorization.basic.web;

import java.io.Serializable;
import java.util.Map;

/**
 * 生成好的令牌信息
 * @author zhouhao
 */
public interface GeneratedToken extends Serializable {
    /**
     * 要响应的数据,可自定义想要的数据给调用者
     * @return {@link Map}
     */
    Map<String,Object> getResponse();

    /**
     * @return 令牌字符串,令牌具有唯一性,不可逆,不包含敏感信息
     */
    String getToken();

    /**
     * @return 令牌有效期（单位毫秒）
     */
    int getTimeout();
}
