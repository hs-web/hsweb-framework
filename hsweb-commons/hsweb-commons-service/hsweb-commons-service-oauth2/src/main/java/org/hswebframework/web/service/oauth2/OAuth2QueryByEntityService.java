package org.hswebframework.web.service.oauth2;

import com.alibaba.fastjson.JSONObject;
import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.commons.entity.PagerResult;
import org.hswebframework.web.service.QueryByEntityService;

import java.util.List;

public interface OAuth2QueryByEntityService<E> extends QueryByEntityService<E>, OAuth2ServiceSupport {

    @Override
    default PagerResult<E> selectPager(Entity param) {
        JSONObject result = createRequest("/", param).get().as(JSONObject.class);
        return PagerResult.of(result.getInteger("total"), result.getJSONArray("data").toJavaList(getEntityType()));
    }

    @Override
    default List<E> select(Entity param) {
        return createRequest("/no-paging", param).get().asList(getEntityType());
    }

    @Override
    default int count(Entity param) {
        return createRequest("/count", param).get().as(Integer.class);
    }

    @Override
    default E selectSingle(Entity param) {
        return createRequest("/single", param).get().as(getEntityType());
    }
}
