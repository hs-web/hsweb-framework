package org.hswebframework.web.system.authorization.defaults.service;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.system.authorization.api.entity.DimensionEntity;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DynamicDimension implements Dimension {

    private String id;
    private String name;
    private DimensionType type;
    private Map<String, Object> options;


    public static DynamicDimension of(DimensionEntity entity,
                                      DimensionType type) {
        DynamicDimension dynamicDimension = new DynamicDimension();
        dynamicDimension.setId(entity.getId());
        dynamicDimension.setName(entity.getName());
        dynamicDimension.setType(type);
        Map<String, Object> options = new HashMap<>();
        options.put("parentId", entity.getParentId());
        options.put("path", entity.getPath());
        dynamicDimension.setOptions(options);
        return dynamicDimension;

    }
}
