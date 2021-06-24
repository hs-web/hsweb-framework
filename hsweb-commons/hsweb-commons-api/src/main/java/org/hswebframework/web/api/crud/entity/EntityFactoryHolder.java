package org.hswebframework.web.api.crud.entity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Component
@Slf4j
public final class EntityFactoryHolder {

    static EntityFactory FACTORY;

    public static EntityFactory get() {
        if (FACTORY == null) {
            throw new IllegalStateException("EntityFactory Not Ready Yet");
        }
        return FACTORY;
    }

    public static <T> T newInstance(Class<T> type,
                                    Supplier<T> mapper) {
        if (FACTORY != null) {
            return FACTORY.newInstance(type);
        }
        return mapper.get();
    }

}
