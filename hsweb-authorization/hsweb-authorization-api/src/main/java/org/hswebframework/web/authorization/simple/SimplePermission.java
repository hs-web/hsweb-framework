package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;

import java.util.Collections;
import java.util.HashSet;
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


    public Set<String> getActions() {
        if (actions == null) {
            actions = new java.util.HashSet<>();
        }
        return actions;
    }

    public Set<DataAccessConfig> getDataAccesses() {
        if (dataAccesses == null) {
            dataAccesses = new java.util.HashSet<>();
        }
        return dataAccesses;
    }
}
