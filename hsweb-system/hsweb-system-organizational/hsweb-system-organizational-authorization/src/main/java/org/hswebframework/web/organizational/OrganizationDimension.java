package org.hswebframework.web.organizational;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hswebframework.web.authorization.DimensionType;

@Getter
@AllArgsConstructor
public enum OrganizationDimension implements DimensionType {
    
    district("行政区"),
    organization("机构"),
    department("部门"),
    position("岗位"),
    person("人员"),

    ;


    private String name;

    @Override
    public String getId() {
        return name();
    }


}
