package org.hswebframework.web.eventbus.executor;

import lombok.Setter;
import org.hswebframework.web.eventbus.EventListenerDefine;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
public class DefaultEventExecutor implements EventListenerExecutor {
    @Setter
    private List<EventExecuteTaskSupplier> suppliers = new ArrayList<>();

    @Override
    public void doExecute(EventListenerDefine define, Object event) {
        suppliers.stream().filter(supplier -> supplier.isSupport(define))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("不支持的listener定义:" + define))
                .get(define.getListener(), event)
                .run();
    }

}
