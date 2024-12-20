package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.DimensionType;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@EqualsAndHashCode
public class SimpleDimension implements Dimension {

    private String id;

    private String name;

    private DimensionType type;

    private Map<String,Object> options;


}
