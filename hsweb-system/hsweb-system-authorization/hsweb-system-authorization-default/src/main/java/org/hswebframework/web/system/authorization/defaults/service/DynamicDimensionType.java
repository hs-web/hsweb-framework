package org.hswebframework.web.system.authorization.defaults.service;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;

@Getter
@Setter
public class DynamicDimensionType implements DimensionType {

    private String id;

    private String name;

    private String parentId;

    private String describe;

    public static DimensionType of(DimensionEntity e) {
        DynamicDimensionType type = new DynamicDimensionType();
        type.id = e.getId();
        type.parentId = e.getParentId();
        type.name = e.getName();
        type.describe = e.getDescribe();
        return type;
    }
}
