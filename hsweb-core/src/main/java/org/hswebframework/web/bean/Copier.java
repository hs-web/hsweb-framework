package org.hswebframework.web.bean;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public interface Copier {
    void copy(Object source, Object target, Set<String> ignore, Converter converter);

    default void copy(Object source, Object target, String... ignore){
        copy(source,target,new HashSet<>(Arrays.asList(ignore)),FastBeanCopier.DEFAULT_CONVERT);
    }

}

