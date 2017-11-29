package org.hswebframework.web.service.oauth2;

import com.alibaba.fastjson.JSON;
import org.hswebframework.web.service.UpdateService;

import java.util.List;

public interface OAuth2UpdateService<E, PK> extends UpdateService<E, PK>, OAuth2ServiceSupport {

    @Override
    default int updateByPk(PK id, E data) {
        return createRequest("/" + id).requestBody(JSON.toJSONString(data)).put().as(Integer.class);
    }

    @Override
    default int updateByPk(List<E> data) {
        return createRequest("/batch").requestBody(JSON.toJSONString(data)).put().as(Integer.class);
    }

    @Override
    default PK saveOrUpdate(E e) {
        return createRequest("/").requestBody(JSON.toJSONString(e)).patch().as(getPrimaryKeyType());
    }
}
