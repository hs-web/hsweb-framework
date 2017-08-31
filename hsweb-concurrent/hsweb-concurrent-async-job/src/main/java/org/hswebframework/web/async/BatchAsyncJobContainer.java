package org.hswebframework.web.async;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface BatchAsyncJobContainer {

    <V> BatchAsyncJobContainer submit(Callable<V> callable);

    List<Object> getResult() throws Exception;
}
