package org.hswebframework.web.authorization.simple;

import lombok.*;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.access.DataAccessConfig;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author zhouhao
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "dataAccesses")
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

    @Override
    public Permission copy(Predicate<String> actionFilter,
                           Predicate<DataAccessConfig> dataAccessFilter) {
        SimplePermission permission = new SimplePermission();

        permission.setId(id);
        permission.setName(name);
        permission.setActions(getActions().stream().filter(actionFilter).collect(Collectors.toSet()));
        permission.setDataAccesses(getDataAccesses().stream().filter(dataAccessFilter).collect(Collectors.toSet()));
        if (options != null) {
            permission.setOptions(new HashMap<>(options));
        }
        return permission;
    }

    public Permission copy() {
        return copy(action -> true, conf -> true);
    }

    @Override
    public String toString() {
        return id + (CollectionUtils.isNotEmpty(actions) ? ":" + String.join(",", actions) : "");
    }
}
