package org.hswebframework.web.authorization.basic.web;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by zhouhao on 2017/8/30.
 */
public interface TokenResult extends Serializable {
    Map<String,Object> getResponse();

    String getToken();

    int getTimeout();
}
