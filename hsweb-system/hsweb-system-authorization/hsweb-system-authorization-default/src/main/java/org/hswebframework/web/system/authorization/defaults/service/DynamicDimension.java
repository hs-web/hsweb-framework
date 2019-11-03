package org.hswebframework.web.system.authorization.defaults.service;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionType;
import org.hswebframework.web.system.authorization.api.entity.DimensionUserEntity;

import java.util.Map;

@Getter
@Setter
public class DynamicDimension implements Dimension {

    private String id;
    private String name;
    private DimensionType type;
    private Map<String, Object> options;


    public static DynamicDimension of(DimensionUserEntity entity,
                                      DimensionType type){
        DynamicDimension dynamicDimension=new DynamicDimension();
        dynamicDimension.setId(entity.getDimensionId());
        dynamicDimension.setName(entity.getDimensionName());
        dynamicDimension.setType(type);
        return dynamicDimension;

    }
}
