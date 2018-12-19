package org.hswebframework.web.concurrent.counter;

import lombok.SneakyThrows;

import java.util.function.Supplier;

public interface BloomFilter {

    boolean put(String unique);

    boolean contains(String unique);

    @SneakyThrows
    default void tryPut(String unique, Supplier<? extends Exception> supplier) {
        if (!put(unique)) {
            throw supplier.get();
        }
    }
}
