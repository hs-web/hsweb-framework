package org.hswebframework.web.authorization.simple;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionType;

import java.util.Map;

@Getter
@Setter
public class SimpleDimension implements Dimension {

    private String id;

    private String name;

    private DimensionType type;

    private Map<String,Object> options;

    public boolean typeIs(DimensionType type) {
        return this.type == type || this.type.getId().equals(type.getId());
    }

    public boolean typeIs(String type) {
        return this.type.getId().equals(type);
    }
}
