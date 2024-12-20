package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.hswebframework.web.authorization.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class SimpleUser implements User {

    @Serial
    private static final long serialVersionUID = 2194541828191869091L;

    private String id;

    private String username;

    private String name;

    private String userType;

    private Map<String, Object> options;
}
