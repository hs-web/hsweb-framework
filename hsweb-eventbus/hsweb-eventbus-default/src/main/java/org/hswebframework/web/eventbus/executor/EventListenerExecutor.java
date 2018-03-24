package org.hswebframework.web.eventbus.executor;

import org.hswebframework.web.eventbus.EventListener;
import org.hswebframework.web.eventbus.EventListenerDefine;

/**
 * @author zhouhao
 * @since 3.0
 */
public interface EventListenerExecutor {
    void doExecute(EventListenerDefine define, Object event);
}
