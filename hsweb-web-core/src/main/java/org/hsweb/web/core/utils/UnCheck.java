package org.hsweb.web.core.utils;

import java.util.function.Function;
import java.util.function.Supplier;

public interface UnCheck<T> {
    T call() throws Exception;

    static <T> T forThrow(UnCheck<T> unCheck, Function<Exception, ? extends RuntimeException> supplier) {
        try {
            return unCheck.call();
        } catch (Exception e) {
            throw supplier.apply(e);
        }
    }

    static <T> T forDefault(UnCheck<T> unCheck, T defaultValue) {
        return forValue(unCheck, () -> defaultValue);
    }

    static <T> T forNull(UnCheck<T> unCheck) {
        return forDefault(unCheck, null);
    }

    static <T> T forValue(UnCheck<T> unCheck, Supplier<T> whenExceptionValue) {
        try {
            return unCheck.call();
        } catch (Exception e) {
            // throw new RuntimeException(e);
            return whenExceptionValue.get();
        }
    }
}