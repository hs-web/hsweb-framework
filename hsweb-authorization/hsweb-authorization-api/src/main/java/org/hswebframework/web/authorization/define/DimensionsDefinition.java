package org.hswebframework.web.authorization.define;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.Predicate;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.annotation.Logical;
import reactor.function.Predicate3;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Getter
@Setter
public class DimensionsDefinition {

    private Map<String, DimensionDefinition> dimensionsMapping = new ConcurrentHashMap<>();

    private Logical logical = Logical.DEFAULT;

    private String description;

    public Set<DimensionDefinition> getDimensions() {
        return new HashSet<>(dimensionsMapping.values());
    }

    public void clear() {
        dimensionsMapping.clear();
    }

    public void addDimension(DimensionDefinition definition) {
        DimensionDefinition old = dimensionsMapping.putIfAbsent(definition.getTypeId(), definition);
        if (old != null) {
            old.addDimensionI(definition.getDimensionId());
        }
    }

    public boolean isEmpty() {
        return MapUtils.isEmpty(this.dimensionsMapping);
    }

    public boolean hasDimension(Dimension dimension) {
        DimensionDefinition def = dimensionsMapping.get(dimension.getType().getId());
        return def != null && def.hasDimension(dimension.getId());
    }

    public boolean hasDimension(Predicate3<String,Logical, Set<String>> filter) {
        if (logical == Logical.AND) {
            return dimensionsMapping
                .values()
                .stream()
                .allMatch(e -> e.hasDimension(filter));
        } else {
            return dimensionsMapping
                .values()
                .stream()
                .anyMatch(e -> e.hasDimension(filter));
        }

    }

    public boolean hasDimension(List<Dimension> dimensions) {

        if (logical == Logical.AND) {
            return dimensions.stream().allMatch(this::hasDimension);
        }

        return dimensions.stream().anyMatch(this::hasDimension);
    }

    @Override
    public String toString() {
        return dimensionsMapping
            .values()
            .stream()
            .map(d -> String.join(",", d.getDimensionId()) + "@" + d.getTypeId())
            .collect(Collectors.joining(";"));
    }
}
