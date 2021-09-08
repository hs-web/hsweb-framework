package org.hswebframework.web.crud.events;

import lombok.Getter;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.springframework.context.ApplicationEvent;

@Getter
public class EntityDDLEvent<E> extends ApplicationEvent {
    private final Class<E> type;

    private final RDBTableMetadata table;

    public EntityDDLEvent(Object source,Class<E> type,RDBTableMetadata table) {
        super(source);
        this.type=type;
        this.table=table;
    }
}
