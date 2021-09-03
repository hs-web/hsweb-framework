package org.hswebframework.web.crud.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;

@AllArgsConstructor
@Getter
public class EntityDDLEvent<E> {
    private Class<E> type;

    private RDBTableMetadata table;
}
