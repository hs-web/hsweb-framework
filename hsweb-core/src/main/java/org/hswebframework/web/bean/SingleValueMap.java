package org.hswebframework.web.bean;

import java.util.*;

public class SingleValueMap<K, V> implements Map<K, V> {
    private K key;
    private V value;

    @Override
    public int size() {
        return value == null ? 0 : 1;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return Objects.equals(this.key, key);
    }

    @Override
    public boolean containsValue(Object value) {
        return Objects.equals(this.value, value);
    }

    @Override
    public V get(Object key) {
        return null;
    }

    @Override
    public V put(K key, V value) {
        this.key = key;
        V old = this.value;
        this.value = value;
        return old;
    }

    @Override
    public V remove(Object key) {
        if (Objects.equals(key, this.key)) {
            V old = this.value;
            this.value = null;
            return old;
        }
        return null;
    }

    @Override
    public void putAll(Map<? extends K,? extends V> m) {
        if (m.size() > 0) {
            Map.Entry<? extends K,? extends V> entry = m.entrySet().iterator().next();
            this.key = entry.getKey();
            this.value = entry.getValue();
        }
    }

    @Override
    public void clear() {
        this.key = null;
        this.value = null;
    }

    @Override
    public Set<K> keySet() {
        return key == null ? Collections.emptySet() : Collections.singleton(key);
    }

    @Override
    public Collection<V> values() {
        return value == null ? Collections.emptySet() : Collections.singleton(value);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return key == null ? Collections.emptySet() : Collections.singleton(
                new Entry<K, V>() {
                    @Override
                    public K getKey() {
                        return key;
                    }

                    @Override
                    public V getValue() {
                        return value;
                    }

                    @Override
                    public V setValue(V value) {
                        V old = SingleValueMap.this.value;
                        SingleValueMap.this.value = value;
                        return old;
                    }
                }
        );
    }

    public V getValue() {
        return value;
    }

    public K getKey() {
        return key;
    }
}
