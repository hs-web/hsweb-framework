package org.hswebframework.web.service.oauth2;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.service.InsertService;

public interface OAuth2InsertService<E, PK> extends InsertService<E, PK>, OAuth2ServiceSupport {
    @Override
    default PK insert(E data) {
        return createRequest("/").requestBody(JSON.toJSONString(data)).post().as(getPrimaryKeyType());
    }
}
