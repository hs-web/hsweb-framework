package org.hswebframework.web.service.form.simple.validator;

import org.hswebframework.web.commons.bean.ValidateBean;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 * @since 3.0.0
 */
public interface MapBean extends Map<String, Object>, ValidateBean {

    @Override
    default int size() {
        return keySet().size();
    }

    @Override
    default boolean isEmpty() {
        return values().isEmpty();
    }

    @Override
    default boolean containsKey(Object key) {
        return keySet().contains(key);
    }

    @Override
    default boolean containsValue(Object value) {
        return values().contains(value);
    }

    @Override
    default Set<Entry<String, Object>> entrySet() {
        return keySet()
                .stream()
                .map(key -> new Entry<String, Object>() {
                    @Override
                    public String getKey() {
                        return key;
                    }

                    @Override
                    public Object getValue() {
                        return getProperty(key);
                    }

                    @Override
                    public Object setValue(Object value) {
                        Object old = getValue();
                        setProperty(key, value);
                        return old;
                    }

                    @Override
                    public boolean equals(Object o) {
                        return getValue() != null && getValue().equals(o);
                    }

                    @Override
                    public int hashCode() {
                        return getValue() == null ? 0 : getValue().hashCode();
                    }
                })
                .collect(Collectors.toSet());
    }

    @Override
    default void putAll(Map<? extends String, ?> m) {
        m.forEach(this::setProperty);
    }

    default void clear() {
        for (String property : keySet()) {
            setProperty(property, null);
        }
    }

    @Override
    default Collection<Object> values() {
        return keySet()
                .stream()
                .map(this::getProperty)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    Set<String> keySet();

    @Override
    default Object get(Object key) {
        return getProperty(String.valueOf(key));
    }

    @Override
    default Object put(String key, Object value) {
        Object old = get(key);
        setProperty(key, value);
        return old;
    }

    void setProperty(String key, Object value);

    Object getProperty(String key);
}
