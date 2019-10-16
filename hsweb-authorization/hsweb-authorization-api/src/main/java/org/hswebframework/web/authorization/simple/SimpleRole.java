package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.hswebframework.web.authorization.Dimension;
import org.hswebframework.web.authorization.Role;

import java.io.Serializable;
import java.util.Map;

/**
 * @author zhouhao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleRole implements Role {

    private static final long serialVersionUID = 7460859165231311347L;

    private String id;

    private String name;

    private Map<String, Object> options;

    public static Role of(Dimension dimension) {
        return SimpleRole.builder()
                .name(dimension.getName())
                .id(dimension.getId())
                .options(dimension.getOptions())
                .build();
    }
}
