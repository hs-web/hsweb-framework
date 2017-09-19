package org.hswebframework.web;

import java.util.*;
import java.util.function.Supplier;

/**
 * List工具，用于构建list等操作
 * <pre>
 *     Lists.buildList("1","2")
 *     .add("3")
 *     .add("4","5","6")
 *     .get();
 * </pre>
 *
 * @author zhouhao
 */
public class Lists {

    public static <V> ListBuilder<V> buildList(Supplier<List<V>> supplier) {
        return buildList(supplier.get());
    }

    public static <V> ListBuilder<V> buildList(V... array) {
        return buildList(array.length == 0 ? new ArrayList<>() : new ArrayList<>(Arrays.asList(array)));
    }

    public static <V> ListBuilder<V> buildList(List<V> target) {
        return new ListBuilder<>(target);
    }

    public static class ListBuilder<V> {
        private final List<V> target;

        private ListBuilder(List<V> target) {
            Objects.requireNonNull(target);
            this.target = target;
        }

        public ListBuilder<V> add(V value, V... more) {
            this.target.add(value);
            if (more.length > 0) {
                addAll(Arrays.asList(more));
            }
            return this;
        }


        public ListBuilder<V> addAll(Collection<V> value) {
            this.target.addAll(value);
            return this;
        }

        public List<V> get() {
            return target;
        }

    }

}
