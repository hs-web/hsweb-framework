package org.hswebframework.web.async;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author zhouhao
 */
public interface BatchAsyncJobContainer {

    default <V> BatchAsyncJobContainer submit(Callable<V> callable) {
        submit(callable, false);
        return this;
    }

    <V> BatchAsyncJobContainer submit(Callable<V> callable, boolean enableTransaction);


    List<Object> getResult() throws Exception;
}
