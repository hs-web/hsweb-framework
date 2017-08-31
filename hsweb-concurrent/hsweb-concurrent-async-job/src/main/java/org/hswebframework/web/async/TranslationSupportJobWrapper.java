package org.hswebframework.web.async;

import java.util.concurrent.Callable;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface TranslationSupportJobWrapper {
    <V> TranslationSupportJob<V> wrapper(Callable<V> callable);
}
