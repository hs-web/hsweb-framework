package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;

import java.util.Set;

/**
 * @author zhouhao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimplePermission implements Permission {

    private static final long serialVersionUID = 7587266693680162184L;

    private String id;

    private Set<String> actions;

    private Set<DataAccessConfig> dataAccesses;
}
