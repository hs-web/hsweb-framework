package org.hswebframework.web.authorization.define;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.authorization.annotation.Logical;
import org.hswebframework.web.bean.FastBeanCopier;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode(of = "typeId")
public class DimensionDefinition {

    private String typeId;

    private String typeName;

    private Set<String> dimensionId = new HashSet<>();

    private Logical logical = Logical.DEFAULT;

    public boolean hasDimension(String id) {
        return dimensionId.contains(id);
    }

    public DimensionDefinition copy() {
        return FastBeanCopier.copy(this, DimensionDefinition::new);
    }
}
