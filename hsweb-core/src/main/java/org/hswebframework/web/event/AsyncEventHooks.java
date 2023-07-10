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
        if (hooksList.size() == 1) {
            return hooksList.getFirst().hookFirst(event, publisher);
        }
        return Flux.fromIterable(hooksList)
                   .flatMap(hook -> hook.hookFirst(event, publisher))
                   .then();
    }

    static Mono<?> hookAsync(AsyncEvent event, Mono<?> publisher) {
        LinkedList<AsyncEventHook> hooksList = hooks.getIfExists();
        if (hooksList == null) {
            return publisher;
        }
        if (hooksList.size() == 1) {
            return hooksList.getFirst().hookAsync(event, publisher);
        }
        return Flux.fromIterable(hooksList)
                   .flatMap(hook -> hook.hookAsync(event, publisher))
                   .then();
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
