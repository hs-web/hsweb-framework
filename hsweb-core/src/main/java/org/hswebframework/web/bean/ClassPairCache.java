package org.hswebframework.web.bean;

import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.BiFunction;

/**
 * Class-pair cache with allocation-free hits and compact rows for sparse type pairs.
 * Values remain strongly cached until full or classloader-scoped clearing.
 */
final class ClassPairCache<V> {
    private static final int SMALL_TARGET_LIMIT = 8;
    private final Map<Class<?>, Row<V>> rows = new ConcurrentHashMap<>();
    private final Map<Key, Creation<V>> creating = new ConcurrentHashMap<>();

    V get(Class<?> source, Class<?> target) {
        Row<V> row = rows.get(source);
        return row == null ? null : row.get(target);
    }

    V computeIfAbsent(Class<?> source, Class<?> target, BiFunction<Class<?>, Class<?>, V> factory) {
        V cached = get(source, target);
        if (cached != null) {
            return cached;
        }
        Key key = new Key(source, target);
        Creation<V> candidate = new Creation<>(this, source, target, factory);
        Creation<V> previous = creating.putIfAbsent(key, candidate);
        Creation<V> creation = previous == null ? candidate : previous;
        if (previous != null && creation.owner == Thread.currentThread() && !creation.task.isDone()) {
            throw new IllegalStateException("Recursive cache creation: " + source + " => " + target);
        }
        try {
            if (previous == null) {
                creation.task.run();
            }
            return creation.await();
        } finally {
            if (previous == null) {
                creating.remove(key, creation);
            }
        }
    }

    void clear() {
        creating.values().forEach(Creation::invalidate);
        rows.clear();
    }

    void clear(ClassLoader loader) {
        if (loader == null) {
            clear();
            return;
        }
        creating.forEach((key, creation) -> {
            if (key.involves(loader)) {
                creation.invalidate();
            }
        });
        rows.forEach((source, row) -> {
            if (FastBeanCopierSupport.isClassLoaderMatch(source, loader)) {
                rows.remove(source, row);
                return;
            }
            Row<V> filtered = row.without(loader);
            // Filter outside the outer map's locks; factories may access other cache rows.
            if (filtered == null) {
                rows.remove(source, row);
            } else if (filtered != row) {
                rows.replace(source, row, filtered);
            }
        });
    }

    int size() {
        return rows.values().stream().mapToInt(Row::size).sum();
    }

    private V putIfAbsent(Class<?> source, Class<?> target, V value) {
        return rows.computeIfAbsent(source, ignore -> new Row<>()).putIfAbsent(target, value);
    }

    private static final class Key {
        private final Class<?> source;
        private final Class<?> target;

        private Key(Class<?> source, Class<?> target) {
            this.source = source;
            this.target = target;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key && source == ((Key) other).source && target == ((Key) other).target;
        }

        @Override
        public int hashCode() {
            return 31 * System.identityHashCode(source) + System.identityHashCode(target);
        }

        private boolean involves(ClassLoader loader) {
            return FastBeanCopierSupport.isClassLoaderMatch(source, loader)
                || FastBeanCopierSupport.isClassLoaderMatch(target, loader);
        }
    }

    private static final class Creation<V> {
        private final Thread owner = Thread.currentThread();
        private final FutureTask<V> task;
        private boolean cacheable = true;

        private Creation(ClassPairCache<V> cache, Class<?> source, Class<?> target,
                         BiFunction<Class<?>, Class<?>, V> factory) {
            task = new FutureTask<>(() -> {
                V cached = cache.get(source, target);
                if (cached != null) {
                    return cached;
                }
                // Factories run outside row/map locks, allowing independent and recursive type-pair creation.
                V value = factory.apply(source, target);
                synchronized (this) {
                    return value != null && cacheable ? cache.putIfAbsent(source, target, value) : value;
                }
            });
        }

        private synchronized void invalidate() {
            // A factory already in progress may return to its caller, but must not repopulate a cleared cache.
            cacheable = false;
        }

        @SneakyThrows
        private V await() {
            boolean interrupted = false;
            try {
                while (true) {
                    try {
                        return task.get();
                    } catch (InterruptedException ignore) {
                        interrupted = true;
                    } catch (ExecutionException failure) {
                        throw failure.getCause();
                    }
                }
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private static final class Entry<V> {
        private final Class<?> target;
        private final V value;

        private Entry(Class<?> target, V value) {
            this.target = target;
            this.value = value;
        }
    }

    private static final class Row<V> {
        // firstTarget is assigned once, before the volatile firstValue publishes the pair.
        private Class<?> firstTarget;
        private volatile V firstValue;
        private volatile Object additional;

        @SuppressWarnings("unchecked")
        private V get(Class<?> target) {
            V first = firstValue;
            if (first != null && target == firstTarget) {
                return first;
            }
            Object entries = additional;
            if (entries instanceof Map) {
                return ((Map<Class<?>, V>) entries).get(target);
            }
            if (entries != null) {
                for (Entry<V> entry : (Entry<V>[]) entries) {
                    if (entry.target == target) {
                        return entry.value;
                    }
                }
            }
            return null;
        }

        private synchronized V putIfAbsent(Class<?> target, V value) {
            V cached = get(target);
            if (cached != null) {
                return cached;
            }
            put(target, value);
            return value;
        }

        @SuppressWarnings("unchecked")
        private void put(Class<?> target, V value) {
            if (firstValue == null) {
                firstTarget = target;
                firstValue = value;
                return;
            }
            Object entries = additional;
            if (entries instanceof Map) {
                ((Map<Class<?>, V>) entries).put(target, value);
                return;
            }
            Entry<V>[] previous = entries == null ? (Entry<V>[]) new Entry[0] : (Entry<V>[]) entries;
            if (previous.length >= SMALL_TARGET_LIMIT) {
                Map<Class<?>, V> expanded = new ConcurrentHashMap<>(previous.length + 1);
                for (Entry<V> entry : previous) {
                    expanded.put(entry.target, entry.value);
                }
                expanded.put(target, value);
                additional = expanded;
            } else {
                Entry<V>[] expanded = (Entry<V>[]) new Entry[previous.length + 1];
                System.arraycopy(previous, 0, expanded, 0, previous.length);
                expanded[previous.length] = new Entry<>(target, value);
                additional = expanded;
            }
        }

        @SuppressWarnings("unchecked")
        private int size() {
            Object entries = additional;
            return (firstValue == null ? 0 : 1) + (entries == null ? 0
                : entries instanceof Map ? ((Map<?, ?>) entries).size() : ((Entry<V>[]) entries).length);
        }

        @SuppressWarnings("unchecked")
        private synchronized Row<V> without(ClassLoader loader) {
            if (!involves(loader)) {
                return this;
            }
            List<Entry<V>> retained = new ArrayList<>();
            boolean removed = false;
            if (firstValue != null) {
                if (FastBeanCopierSupport.isClassLoaderMatch(firstTarget, loader)) {
                    removed = true;
                } else {
                    retained.add(new Entry<>(firstTarget, firstValue));
                }
            }
            Object entries = additional;
            if (entries instanceof Map) {
                for (Map.Entry<Class<?>, V> entry : ((Map<Class<?>, V>) entries).entrySet()) {
                    if (FastBeanCopierSupport.isClassLoaderMatch(entry.getKey(), loader)) {
                        removed = true;
                    } else {
                        retained.add(new Entry<>(entry.getKey(), entry.getValue()));
                    }
                }
            } else if (entries != null) {
                for (Entry<V> entry : (Entry<V>[]) entries) {
                    if (FastBeanCopierSupport.isClassLoaderMatch(entry.target, loader)) {
                        removed = true;
                    } else {
                        retained.add(entry);
                    }
                }
            }
            if (!removed) {
                return this;
            }
            if (retained.isEmpty()) {
                return null;
            }
            Row<V> filtered = new Row<>();
            for (Entry<V> entry : retained) {
                filtered.put(entry.target, entry.value);
            }
            return filtered;
        }

        @SuppressWarnings("unchecked")
        private boolean involves(ClassLoader loader) {
            if (FastBeanCopierSupport.isClassLoaderMatch(firstTarget, loader)) {
                return true;
            }
            Object entries = additional;
            if (entries instanceof Map) {
                for (Class<?> target : ((Map<Class<?>, V>) entries).keySet()) {
                    if (FastBeanCopierSupport.isClassLoaderMatch(target, loader)) {
                        return true;
                    }
                }
            } else if (entries != null) {
                for (Entry<V> entry : (Entry<V>[]) entries) {
                    if (FastBeanCopierSupport.isClassLoaderMatch(entry.target, loader)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
