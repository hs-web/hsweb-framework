package org.hswebframework.web.crud.events;

import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Awaited prepare phase of a partial update. Before/after retain mapped-column views;
 * submittedValues also exposes original virtual properties for validation and auditing.
 * @see org.hswebframework.web.crud.annotation.EnableEntityEvent
 * @param <E>
 */
@Getter
public class EntityPrepareModifyEvent<E> extends DefaultAsyncEvent implements Serializable{

    private static final long serialVersionUID = -7158901204884303777L;

    private final List<E> before;

    private final List<E> after;

    private final Class<E> entityType;

    private final Map<String, Object> submittedValues;

    public EntityPrepareModifyEvent(List<E> before, List<E> after, Class<E> entityType) {
        this(before, after, entityType, Collections.emptyMap());
    }

    /** @since 5.0.3 */
    public EntityPrepareModifyEvent(List<E> before, List<E> after, Class<E> entityType, Map<String, Object> submittedValues) {
        this.before = before;
        this.after = after;
        this.entityType = entityType;
        this.submittedValues = Collections.unmodifiableMap(new LinkedHashMap<>(submittedValues));
    }

    @Override
    public String toString() {
        return "EntityPrepareModifyEvent<" + entityType.getSimpleName() + ">\n{\nbefore:" + before + "\nafter: " + after + "\n}";
    }
}
