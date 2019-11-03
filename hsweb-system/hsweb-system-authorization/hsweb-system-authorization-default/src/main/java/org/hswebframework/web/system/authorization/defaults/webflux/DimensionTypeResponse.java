package org.hswebframework.web.system.authorization.defaults.webflux;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hswebframework.web.authorization.DimensionType;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class DimensionTypeResponse {

    private String id;

    private String name;

    public static DimensionTypeResponse of(DimensionType type) {
        return of(type.getId(), type.getName());
    }
}
