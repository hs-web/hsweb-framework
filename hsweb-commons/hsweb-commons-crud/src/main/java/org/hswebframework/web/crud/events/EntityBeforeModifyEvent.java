package org.hswebframework.web.crud.events;

import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.LinkedHashMap;

/**
 * Awaited before phase of a partial update. Before/after retain mapped-column views;
 * submittedValues includes the original virtual-property patch and preserves explicit nulls.
 * @param <E>
 * @see org.hswebframework.web.crud.annotation.EnableEntityEvent
 */
@Getter
public class EntityBeforeModifyEvent<E> extends DefaultAsyncEvent implements Serializable {

    private static final long serialVersionUID = -7158901204884303777L;

    private final List<E> before;

    private final List<E> after;

    private final Class<E> entityType;

    private final Map<String, Object> submittedValues;

    public EntityBeforeModifyEvent(List<E> before, List<E> after, Class<E> entityType) {
        this(before, after, entityType, Collections.emptyMap());
    }

    /** @since 5.0.3 */
    public EntityBeforeModifyEvent(List<E> before, List<E> after, Class<E> entityType, Map<String, Object> submittedValues) {
        this.before = before;
        this.after = after;
        this.entityType = entityType;
        this.submittedValues = Collections.unmodifiableMap(new LinkedHashMap<>(submittedValues));
    }

    @Override
    public String toString() {
        return "EntityBeforeModifyEvent<" + entityType.getSimpleName() + ">\n{\nbefore:" + before + "\nafter: " + after + "\n}";
    }
}
