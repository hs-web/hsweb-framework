package org.hswebframework.web.service.oauth2;

import org.hswebframework.web.service.QueryService;

import java.util.List;

public interface OAuth2QueryService<E, PK> extends OAuth2ServiceSupport, QueryService<E, PK> {

    @Override
    default E selectByPk(PK id) {
        return createRequest("/" + id).get().as(getEntityType());
    }

    @Override
    default List<E> select() {
        return createRequest("/all").get().asList(getEntityType());
    }

    @Override
    default List<E> selectByPk(List<PK> id) {
        return createRequest("/ids")
                .param("ids", id.stream()
                        .map(String::valueOf)
                        .reduce((id1, id2) -> String.join(",", id1, id2))
                        .orElse(""))
                .get()
                .asList(getEntityType());
    }

    @Override
    default int count() {
        return createRequest("/count")
                .get()
                .as(Integer.class);
    }
}
