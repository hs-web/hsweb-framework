package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;

import java.util.*;

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

    private String name;

    private Set<String> actions;

    private Set<DataAccessConfig> dataAccesses;

    private Map<String, Object> options;

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

    public Permission copy() {
        SimplePermission permission = new SimplePermission();

        permission.setId(id);
        permission.setName(name);
        permission.setActions(new HashSet<>(getActions()));
        permission.setDataAccesses(new HashSet<>(getDataAccesses()));
        if (options != null) {
            permission.setOptions(new HashMap<>(options));
        }
        return permission;
    }
}
