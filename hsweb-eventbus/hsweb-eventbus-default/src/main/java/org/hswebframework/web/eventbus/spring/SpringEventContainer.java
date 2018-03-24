package org.hswebframework.web.eventbus.spring;

import org.hswebframework.web.eventbus.annotation.EventMode;
import org.hswebframework.web.eventbus.executor.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author zhouhao
 * @since 3.0
 */
public class SpringEventContainer extends AbstractEventContainer {
    List<EventExecuteTaskSupplier> suppliers = Arrays.asList(
            new BackGroundEventTaskSupplier(), new AsyncEventTaskSupplier(),new SyncEventListenerExecutor());


    protected boolean hasAsyncTx() {
        return defines.stream()
                .anyMatch(define -> define.getEventMode() == EventMode.ASYNC && define.isTransaction());
    }

    @Override
    protected EventListenerExecutor newExecutor() {
        DefaultEventExecutor eventExecutor = new DefaultEventExecutor();
        List<EventExecuteTaskSupplier> suppliers = new ArrayList<>(this.suppliers);
        if (hasAsyncTx()) {
            // TODO: 18-3-24
        }
        eventExecutor.setSuppliers(suppliers);
        return eventExecutor;
    }

    @Override
    protected void executeAfter(EventListenerExecutor executor) {

    }

}
