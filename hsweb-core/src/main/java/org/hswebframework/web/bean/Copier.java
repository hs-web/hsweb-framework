package org.hswebframework.web.bean;

import com.google.common.collect.Sets;
import reactor.core.Disposable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface Copier extends Disposable {
    void copy(Object source, Object target, Set<String> ignore, Converter converter);

    default void copy(Object source, Object target, String... ignore) {
        copy(source, target, Sets.newHashSet(ignore), FastBeanCopier.DEFAULT_CONVERT);
    }

    @Override
    default void dispose() {

    }

}

