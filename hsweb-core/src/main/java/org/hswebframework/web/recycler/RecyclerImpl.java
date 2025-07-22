package org.hswebframework.web.recycler;

import io.netty.util.concurrent.FastThreadLocal;
import lombok.AllArgsConstructor;
import org.jctools.queues.MpmcArrayQueue;
import reactor.core.scheduler.Schedulers;
import reactor.function.Function6;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Consumer;
import java.util.function.Supplier;

class RecyclerImpl<T> extends FastThreadLocal<RecyclerImpl.ThreadLocalRecyclable<T>> implements Recycler<T> {

    private final Supplier<T> factory;
    private final Consumer<T> rest;

    private final Queue<T> queue;
    private final int maxSize;

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
        this.maxSize = size;
        this.queue = new MpmcArrayQueue<>(size);
    }

    @Override
    protected ThreadLocalRecyclable<T> initialValue() throws Exception {
        return new ThreadLocalRecyclable<T>(factory.get(), false);
    }

    @Override
    protected void onRemoval(ThreadLocalRecyclable<T> value) {
        rest.accept(value.value);
    }

    @Override
    public <A, A1, A2, A3, A4, R> R doWith(A arg0, A1 arg1, A2 arg2, A3 arg3, A4 arg4, Function6<T, A, A1, A2, A3, A4, R> call) {
        // 非阻塞线程里 优先使用ThreadLocal池
        if (Schedulers.isInNonBlockingThread()) {
            ThreadLocalRecyclable<T> ref = this.get();
            // 使用中,回调里又执行了?
            if (!ref.using) {
                try {
                    ref.using = true;
                    return call.apply(ref.value, arg0, arg1, arg2, arg3, arg4);
                } finally {
                    ref.using = false;
                    rest.accept(ref.value);
                }
            }
        }
        // 在阻塞线程中,使用队列的方式,防止在虚拟线程等场景下创建大量对象导致性能反而降低.
        T t = queue.poll();
        boolean recycle = true;
        if (t == null) {
            t = factory.get();
            // 如果队列已满，不回收新创建的对象
            if (queue.size() >= maxSize) {
                recycle = false;
            }
        }
        try {
            return call.apply(t, arg0, arg1, arg2, arg3, arg4);
        } finally {
            rest.accept(t);
            if (recycle) {
                queue.offer(t);
            }
        }
    }


    @Override
    public Recyclable<T> take(boolean synchronous) {
        // 同步的,尝试使用ThreadLocal
        if (synchronous && Schedulers.isInNonBlockingThread()) {
            ThreadLocalRecyclable<T> ref = this.get();
            if (!ref.using) {
                ref.using = true;
                return ref;
            }
        }
        T t = queue.poll();
        boolean recycle = true;
        if (t == null) {
            t = factory.get();
            // 如果队列已满，不回收新创建的对象
            if (queue.size() >= maxSize) {
                recycle = false;
            }
        }
        return new QueueRecyclable<>(this, recycle, t);
    }

    @AllArgsConstructor
    static class QueueRecyclable<T> implements Recyclable<T> {
        @SuppressWarnings("all")
        static final AtomicReferenceFieldUpdater<QueueRecyclable, Object>
            VALUE = AtomicReferenceFieldUpdater.newUpdater(QueueRecyclable.class, Object.class, "value");

        final RecyclerImpl<T> main;
        final boolean doRecycle;
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
                main.rest.accept(val);
                if (doRecycle) {
                    main.queue.offer(val);
                }
            }
        }
    }

    @AllArgsConstructor
   static class ThreadLocalRecyclable<T> implements Recyclable<T> {
        private T value;
        private boolean using;

        @Override
        public T get() {
            return value;
        }

        @Override
        public void recycle() {
            using = false;
        }
    }
}
