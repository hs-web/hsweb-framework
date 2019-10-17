package org.hswebframework.web.authorization;

import org.hswebframework.web.authorization.simple.SimpleDimension;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

public interface Dimension extends Serializable {
    String getId();

    String getName();

    DimensionType getType();

    Map<String, Object> getOptions();

    default <T> Optional<T> getOption(String key) {
        return Optional.ofNullable(getOptions())
                .map(ops -> ops.get(key))
                .map(o -> (T) o);
    }

    default boolean typeIs(DimensionType type) {
        return this.getType() == type || this.getType().getId().equals(type.getId());
    }

    default boolean typeIs(String type) {
        return this.getType().getId().equals(type);
    }

    static Dimension of(String id, String name, DimensionType type) {
        return of(id, name, type, null);
    }

    static Dimension of(String id, String name, DimensionType type, Map<String, Object> options) {
        return SimpleDimension.of(id, name, type, options);
    }
}
