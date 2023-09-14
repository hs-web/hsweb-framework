package org.hswebframework.web.event;

import io.netty.util.concurrent.FastThreadLocal;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.LinkedList;

public class AsyncEventHooks {

    private static final FastThreadLocal<LinkedList<AsyncEventHook>> hooks = new FastThreadLocal<LinkedList<AsyncEventHook>>() {
        @Override
        protected LinkedList<AsyncEventHook> initialValue() {
            return new LinkedList<>();
        }
    };

    public static AutoUnbindable bind(AsyncEventHook hook) {
        LinkedList<AsyncEventHook> list = hooks.get();
        list.add(hook);
        return () -> list.removeLastOccurrence(hook);
    }

    static Mono<?> hookFirst(AsyncEvent event, Mono<?> publisher) {
        LinkedList<AsyncEventHook> hooksList = hooks.getIfExists();
        if (hooksList == null) {
            return publisher;
        }
        for (AsyncEventHook asyncEventHook : hooksList) {
            publisher = asyncEventHook.hookFirst(event, publisher);
        }
        return publisher;
    }

    static Mono<?> hookAsync(AsyncEvent event, Mono<?> publisher) {
        LinkedList<AsyncEventHook> hooksList = hooks.getIfExists();
        if (hooksList == null) {
            return publisher;
        }
        for (AsyncEventHook asyncEventHook : hooksList) {
            publisher = asyncEventHook.hookAsync(event, publisher);
        }
        return publisher;
    }


    public interface AutoUnbindable extends AutoCloseable {
        @Override
        void close();
    }

    public interface AsyncEventHook {
        default Mono<?> hookAsync(AsyncEvent event, Mono<?> publisher) {
            return publisher;
        }

        default Mono<?> hookFirst(AsyncEvent event, Mono<?> publisher) {
            return publisher;
        }
    }

}
