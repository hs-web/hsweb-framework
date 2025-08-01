package org.hswebframework.web.context;

import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.io.Closeable;

/**
 * 基于 ThreadLocal 的上下文持有器支持实现
 * 适用于传统平台线程环境
 */
public class ThreadLocalContextHolderSupport implements ContextHolder.ContextHolderSupport {

    private static final ThreadLocal<Context> contextHolder = ThreadLocal.withInitial(Context::empty);

    @Override
    public boolean isSupport() {
        return true;
    }

    @Override
    public Closeable makeCurrent(ContextView context) {
        Context previous = contextHolder.get();
        Context newContext = previous.putAll(context);
        contextHolder.set(newContext);

        return () -> contextHolder.set(previous);
    }

    @Override
    public void clean() {
        contextHolder.remove();
    }

    @Override
    public Context current() {
        Context context = contextHolder.get();
        return context != null ? context : Context.empty();
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE; // 最低优先级，作为回退选项
    }
}
