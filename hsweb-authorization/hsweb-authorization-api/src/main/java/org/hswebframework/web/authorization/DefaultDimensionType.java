package org.hswebframework.web.authorization;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DefaultDimensionType implements DimensionType {
    user("用户"),
    role("角色");

    private String name;

    @Override
    public String getId() {
        return name();
    }
}
