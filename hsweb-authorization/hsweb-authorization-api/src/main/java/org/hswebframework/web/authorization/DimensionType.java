package org.hswebframework.web.authorization;

public interface DimensionType {
    String getId();

    String getName();

    default boolean isSameType(DimensionType another) {
        return this == another || isSameType(another.getId());
    }

    default boolean isSameType(String anotherId) {
        return this.getId().equals(anotherId);
    }
}
