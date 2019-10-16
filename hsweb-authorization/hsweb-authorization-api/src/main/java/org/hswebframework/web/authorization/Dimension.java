package org.hswebframework.web.authorization;

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
}
