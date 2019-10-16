package org.hswebframework.web.authorization.define;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.annotation.Logical;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class DimensionsDefinition {

    private List<DimensionDefinition> dimensions = new ArrayList<>();

    private Logical logical = Logical.DEFAULT;

    public void addDimension(DimensionDefinition definition) {
        dimensions.add(definition);
    }

    public boolean hasDimension(Dimension dimension) {
        return dimensions
                .stream()
                .anyMatch(def ->
                        def.getTypeId().equals(dimension.getType().getId())
                                && def.hasDimension(dimension.getId()));
    }

    public boolean hasDimension(List<Dimension> dimensions) {

        if (CollectionUtils.isEmpty(this.dimensions)) {
            return true;
        }
        if (CollectionUtils.isEmpty(this.dimensions)) {
            return false;
        }
        if (logical == Logical.AND) {
            return dimensions.stream().allMatch(this::hasDimension);
        }

        return dimensions.stream().anyMatch(this::hasDimension);
    }
}
