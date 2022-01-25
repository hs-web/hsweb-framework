package org.hswebframework.web.system.authorization.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.event.DefaultAsyncEvent;

@Getter
@AllArgsConstructor
public class DimensionDeletedEvent extends DefaultAsyncEvent {
    private final String dimensionType;
    private final String dimensionId;
}
