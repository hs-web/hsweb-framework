package org.hswebframework.web.bean;


import java.util.*;

/**
 * @author zhouhao
 * @since 3.0.0-RC
 */
public interface ToStringOperator<T> {

    default String toString(T target, String... ignoreProperty) {
        return toString(target, -1, ignoreProperty == null ? new java.util.HashSet<>() : new HashSet<>(Arrays.asList(ignoreProperty)));
    }

    String toString(T target, long features, Set<String> ignoreProperty);
}
