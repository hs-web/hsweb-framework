package org.hswebframework.web.boost.bean;

import java.util.Set;

public interface Copier {
    void copy(Object source, Object target, Set<String> ignore, Converter converter);
}

