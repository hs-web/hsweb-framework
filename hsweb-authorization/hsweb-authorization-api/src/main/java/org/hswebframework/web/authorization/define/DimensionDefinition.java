package org.hswebframework.web.authorization.define;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.bean.FastBeanCopier;
import reactor.function.Predicate3;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

@Getter
@Setter
@EqualsAndHashCode(of = "typeId")
public class DimensionDefinition {

    private String typeId;

    private String typeName;

    private Set<String> dimensionId = new HashSet<>();

    private Logical logical = Logical.DEFAULT;

    public boolean hasDimension(Predicate3<String,Logical, Set<String>> filter) {
        return filter.test(typeId,logical, Collections.unmodifiableSet(dimensionId));
    }

    public boolean hasDimension(Set<String> dimensionIdPredicate) {
        if (logical == Logical.AND) {
            return dimensionIdPredicate.containsAll(dimensionId);
        }
        return dimensionId
            .stream()
            .anyMatch(dimensionIdPredicate::contains);
    }

    public boolean hasDimension(String id) {
        return dimensionId.contains(id);
    }

    public void addDimensionI(Set<String> id) {
        dimensionId.addAll(id);
    }

    public DimensionDefinition copy() {
        return FastBeanCopier.copy(this, DimensionDefinition::new);
    }
}
