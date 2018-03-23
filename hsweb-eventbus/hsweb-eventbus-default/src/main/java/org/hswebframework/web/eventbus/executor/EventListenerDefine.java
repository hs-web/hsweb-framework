package org.hswebframework.web.eventbus.executor;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.eventbus.annotation.EventMode;

import java.util.EventListener;

/**
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
@Builder
public class EventListenerDefine {
    private EventListener listener;

    private EventMode eventMode;

    private boolean transaction;

    private int priority;
}
