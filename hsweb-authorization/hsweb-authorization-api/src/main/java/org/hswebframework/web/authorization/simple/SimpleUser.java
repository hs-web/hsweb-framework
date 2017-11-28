package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.hswebframework.web.authorization.User;

/**
 * @author zhouhao
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimpleUser implements User {

    private static final long serialVersionUID = 2194541828191869091L;

    private String id;

    private String username;

    private String name;
    
    private String type;
}
