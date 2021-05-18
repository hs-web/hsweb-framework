package org.hswebframework.web.system.authorization.api.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.event.DefaultAsyncEvent;

import java.util.List;

@AllArgsConstructor
@Getter
public class DimensionBindEvent extends DefaultAsyncEvent {

    private final String type;

    private final String dimensionId;

    private final List<String> userId;
}
