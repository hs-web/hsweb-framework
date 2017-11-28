package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.hswebframework.web.authorization.Role;

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
}
