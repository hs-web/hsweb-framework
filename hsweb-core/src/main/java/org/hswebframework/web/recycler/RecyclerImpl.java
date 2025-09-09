package org.hswebframework.web.recycler;

import io.netty.util.concurrent.FastThreadLocal;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jctools.queues.MpmcArrayQueue;
import reactor.core.scheduler.Schedulers;
import reactor.function.Function6;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
class RecyclerImpl<T> extends FastThreadLocal<RecyclerImpl.ThreadLocalRecyclable<T>> implements Recycler<T> {

    private final Supplier<T> factory;
    private final Consumer<T> rest;

    private final Queue<T> queue;

    public RecyclerImpl(int size, Supplier<T> factory, Consumer<T> rest) {
        if (size < 2) {
            throw new IllegalArgumentException("size must be at least 2");
        }
        if (factory == null) {
            throw new IllegalArgumentException("factory cannot be null");
        }
        if (rest == null) {
            throw new IllegalArgumentException("rest cannot be null");
        }

        this.factory = factory;
        this.rest = rest;
        this.queue = new MpmcArrayQueue<>(size);
    }

    @Override
    protected ThreadLocalRecyclable<T> initialValue() throws Exception {
        return new ThreadLocalRecyclable<T>(this, factory.get(), null);
    }

    @Override
    protected void onRemoval(ThreadLocalRecyclable<T> value) {
        rest.accept(value.value);
    }

    private void doReset(T val) {
        try {
            rest.accept(val);
        } catch (Throwable e) {
            log.warn("reset object [{}] failed", val, e);
        }
    }

    @Override
    public <A, A1, A2, A3, A4, R> R doWith(A arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, Function6<T, A, A1, A2, A3, A4, R> call) {
        // 非阻塞线程里 优先使用ThreadLocal池
        if (Schedulers.isInNonBlockingThread()) {
            ThreadLocalRecyclable<T> ref = this.get();
            // 使用中,回调里又执行了?
            if (ref.use()) {
                try {
                    return call.apply(ref.value, arg0, arg1, arg2, arg3, arg4);
                } finally {
                    doReset(ref.value);
                    ref.recycle();
                }
            }
        }
        // 在阻塞线程中,使用队列的方式,防止在虚拟线程等场景下创建大量对象导致性能反而降低.
        T t = queue.poll();
        if (t == null) {
            t = factory.get();
        }
        try {
            return call.apply(t, arg0, arg1, arg2, arg3, arg4);
        } finally {
            doReset(t);
            queue.offer(t);
        }
    }


    @Override
    public Recyclable<T> take(boolean synchronous) {
        // 同步的,尝试使用ThreadLocal
        if (synchronous && Schedulers.isInNonBlockingThread()) {
            ThreadLocalRecyclable<T> ref = this.get();
            if (ref.use()) {
                return new OnceRecyclable<>(ref);
            }
        }
        T t = queue.poll();
        if (t == null) {
            t = factory.get();
        }
        return new QueueRecyclable<>(this, t);
    }

    @AllArgsConstructor
    static class OnceRecyclable<T> implements Recyclable<T> {
        @SuppressWarnings("all")
        static final AtomicReferenceFieldUpdater<OnceRecyclable, Recyclable>
            REF = AtomicReferenceFieldUpdater.newUpdater(OnceRecyclable.class, Recyclable.class, "recyclable");

        private volatile Recyclable<T> recyclable;

        @Override
        public T get() {
            @SuppressWarnings("unchecked")
            Recyclable<T> recyclable = REF.get(this);
            if (recyclable == null) {
                throw new IllegalStateException("Object is recycled!");
            }
            return recyclable.get();
        }

        @Override
        public void recycle() {
            @SuppressWarnings("unchecked")
            Recyclable<T> recyclable = REF.getAndSet(this, null);
            if (recyclable != null) {
                recyclable.recycle();
            }
        }
    }

    @AllArgsConstructor
    static class QueueRecyclable<T> implements Recyclable<T> {
        @SuppressWarnings("all")
        static final AtomicReferenceFieldUpdater<QueueRecyclable, Object>
            VALUE = AtomicReferenceFieldUpdater.newUpdater(QueueRecyclable.class, Object.class, "value");

        final RecyclerImpl<T> main;
        volatile T value;

        @Override
        public T get() {
            @SuppressWarnings("all")
            T val = (T) VALUE.get(this);
            if (val == null) {
                throw new IllegalStateException("Object is recycled!");
            }
            return val;
        }

        @Override
        public void recycle() {
            @SuppressWarnings("all")
            T val = (T) VALUE.getAndSet(this, null);
            if (val != null) {
                main.doReset(val);
                main.queue.offer(val);
            }
        }
    }

    @AllArgsConstructor
    static class ThreadLocalRecyclable<T> implements Recyclable<T> {
        @SuppressWarnings("all")
        static final AtomicReferenceFieldUpdater<ThreadLocalRecyclable, Thread>
            USING = AtomicReferenceFieldUpdater.newUpdater(ThreadLocalRecyclable.class, Thread.class, "using");
        private final RecyclerImpl<T> main;
        private final T value;
        private volatile Thread using;

        @Override
        public T get() {
            return value;
        }

        boolean use() {
            return USING.compareAndSet(this, null, Thread.currentThread());
        }

        @Override
        public void recycle() {
            main.doReset(value);
            Thread current = Thread.currentThread();
            Thread hold = USING.getAndSet(this, null);
            if (hold != null) {
                if (hold != current) {
                    log.warn("Recycle object cross thread! request by {},recycle by {}", hold, current);
                }
            }
        }
    }
}
