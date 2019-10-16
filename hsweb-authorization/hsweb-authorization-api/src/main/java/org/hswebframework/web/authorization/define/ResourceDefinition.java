package org.hswebframework.web.authorization.define;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.authorization.annotation.Logical;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
public class ResourceDefinition {
    private String id;

    private String name;

    private String description;

    private List<ResourceActionDefinition> actions=new ArrayList<>();

    @Setter(value = AccessLevel.PRIVATE)
    private volatile Set<String> actionIds;

    private Logical logical = Logical.DEFAULT;

    public void addAction(ResourceActionDefinition action){
        actions.add(action);
    }

    public Optional<ResourceActionDefinition> getAction(String action){
        return actions.stream()
                .filter(act->act.getId().equalsIgnoreCase(action))
                .findAny();
    }

    public Set<String> getActionIds() {
        if (actionIds == null) {
            actionIds = this.actions
                    .stream()
                    .map(ResourceActionDefinition::getId)
                    .collect(Collectors.toSet());
        }
        return actionIds;
    }

    public List<ResourceActionDefinition> getDataAccessAction(){
        return actions.stream()
                .filter(act->act.getDataAccess()!=null)
                .collect(Collectors.toList());
    }

    public boolean hasDataAccessAction(){
        return actions.stream()
                .anyMatch(act->act.getDataAccess()!=null);
    }

    public boolean hasAction(Collection<String> actions) {
        if (CollectionUtils.isEmpty(this.actions)) {
            return true;
        }

        if (CollectionUtils.isEmpty(actions)) {
            return false;
        }

        if (logical == Logical.AND) {
            return getActionIds().containsAll(actions);
        }
        return getActionIds().stream().anyMatch(actions::contains);
    }
}
