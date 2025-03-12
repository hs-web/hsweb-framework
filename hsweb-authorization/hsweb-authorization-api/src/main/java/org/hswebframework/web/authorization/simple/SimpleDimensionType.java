package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.hswebframework.web.authorization.DimensionType;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
public class SimpleDimensionType implements DimensionType, Serializable {
    private static final long serialVersionUID = -6849794470754667710L;

    private String id;

    private String name;

    public static SimpleDimensionType of(String id) {
        return of(id, id);
    }
}
