package org.hswebframework.web.crud.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.ezorm.core.param.QueryParam;
import org.hswebframework.web.api.crud.entity.QueryParamEntity;
import org.hswebframework.web.event.DefaultAsyncEvent;

import java.io.Serializable;
import java.util.List;

/**
 * @see org.hswebframework.web.crud.annotation.EnableEntityEvent
 * @param <E>
 */
@AllArgsConstructor
@Getter
public class EntityBeforeQueryEvent<E> extends DefaultAsyncEvent implements Serializable {

    private final QueryParam param;

    private final Class<E> entityType;

    @Override
    public String toString() {
        return "EntityBeforeQueryEvent<" + entityType.getSimpleName() + ">"+param;
    }
}
