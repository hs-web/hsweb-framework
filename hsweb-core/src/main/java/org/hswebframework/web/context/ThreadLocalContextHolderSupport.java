package org.hswebframework.web.context;

import lombok.extern.slf4j.Slf4j;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.io.Closeable;

/**
 * 基于 ThreadLocal 的上下文持有器支持实现
 * 适用于传统平台线程环境
 */
@Slf4j
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
        Thread bound = Thread.currentThread();

        return () -> {
            Thread current = Thread.currentThread();
            if (current != bound) {
                log.warn("Context holder is cross thread {}=>{} {}", bound, current, context);
            } else {
                contextHolder.set(previous);
            }
        };
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
