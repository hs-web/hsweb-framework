package org.hswebframework.web.eventbus;

import lombok.*;
import org.hswebframework.web.eventbus.annotation.EventMode;


/**
 * @author zhouhao
 * @since 3.0
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventListenerDefine {
    private EventListener listener;

    private EventMode eventMode = EventMode.SYNC;

    private boolean transaction = true;

    private int priority = 0;
}
