package org.hswebframework.web.system.authorization.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class PermissionDimension {

    private String id;

    private Map<String, Object> properties;

    public static PermissionDimension of(String id) {
        return of(id, null);
    }
}
