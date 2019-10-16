package org.hswebframework.web.system.authorization.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.authorization.DimensionType;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class PermissionDimension {

    private String id;

    private DimensionType dimensionType;

    private Map<String, Object> properties;

    public static PermissionDimension of(String id, DimensionType dimensionType) {
        return of(id, dimensionType, null);
    }
}
