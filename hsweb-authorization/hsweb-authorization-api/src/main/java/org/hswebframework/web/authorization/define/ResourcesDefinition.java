package org.hswebframework.web.authorization.define;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Logical;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class ResourcesDefinition {

    private Set<ResourceDefinition> resources = new HashSet<>();

    private Logical logical = Logical.DEFAULT;

    private Phased phased = Phased.before;

    public void addResource(ResourceDefinition resource, boolean merge) {
        ResourceDefinition definition = getResource(resource.getId()).orElse(null);
        if (definition != null) {
            if (merge) {
                resource.getActions()
                        .stream()
                        .map(ResourceActionDefinition::copy)
                        .forEach(definition::addAction);
            } else {
                resources.remove(definition);
            }
        }
        resources.add(resource.copy());

    }


    public Optional<ResourceDefinition> getResource(String id) {
        return resources
                .stream()
                .filter(resource -> resource.getId().equals(id))
                .findAny();
    }

    @JsonIgnore
    public List<ResourceDefinition> getDataAccessResources() {
        return resources
                .stream()
                .filter(ResourceDefinition::hasDataAccessAction)
                .collect(Collectors.toList());
    }

    public boolean hasPermission(Permission permission) {
        if (CollectionUtils.isEmpty(resources)) {
            return true;
        }
        return getResource(permission.getId())
                .filter(resource -> resource.hasAction(permission.getActions()))
                .isPresent();
    }

    public boolean isEmpty() {
        return resources.isEmpty();
    }

    public boolean hasPermission(Authentication authentication) {

        if (CollectionUtils.isEmpty(resources)) {
            return true;
        }

        if (logical == Logical.AND) {
            return resources
                    .stream()
                    .allMatch(resource -> authentication.hasPermission(resource.getId(), resource.getActionIds()));
        }

        return resources
                .stream()
                .anyMatch(resource -> authentication.hasPermission(resource.getId(), resource.getActionIds()));
    }
}
