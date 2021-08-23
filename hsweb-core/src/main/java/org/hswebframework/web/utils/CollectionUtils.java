package org.hswebframework.web.utils;

import reactor.function.Consumer3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class CollectionUtils {

    @SafeVarargs
    public static <A> Map<A, A> pairingArrayMap(A... array) {
        return pairingArray(array, LinkedHashMap::new, Map::put);
    }

    public static <A, T> T pairingArray(A[] array,
                                        Supplier<T> supplier,
                                        Consumer3<T, A, A> mapping) {
        T container = supplier.get();
        for (int i = 0, len = array.length / 2; i < len; i++) {
            mapping.accept(container, array[i * 2], array[i * 2 + 1]);
        }
        return container;
    }
}
