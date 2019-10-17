package org.hswebframework.web.authorization.define;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.hswebframework.web.authorization.Permission;
import org.hswebframework.web.authorization.annotation.Logical;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class ResourcesDefinition {

    private List<ResourceDefinition> resources = new ArrayList<>();

    private Logical logical = Logical.DEFAULT;

    private Phased phased = Phased.before;

    public void addResource(ResourceDefinition resource, boolean merge) {
        ResourceDefinition definition = getResource(resource.getId()).orElse(null);
        if (definition != null) {
            if (merge) {
                resource.getActions().forEach(definition::addAction);
            } else {
                resources.remove(definition);
            }
        }
        resources.add(resource);

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

    public boolean hasPermission(Collection<Permission> permissions) {

        if (CollectionUtils.isEmpty(resources)) {
            return true;
        }
        if (CollectionUtils.isEmpty(permissions)) {
            return false;
        }
        if (permissions.size() == 1) {
            return hasPermission(permissions.iterator().next());
        }

        Map<String, Permission> mappings = permissions.stream().collect(Collectors.toMap(Permission::getId, Function.identity()));

        if (logical == Logical.AND) {
            return resources.stream()
                    .allMatch(resource -> Optional.ofNullable(mappings.get(resource.getId()))
                            .map(per -> resource.hasAction(per.getActions()))
                            .orElse(false));
        }

        return resources.stream()
                .anyMatch(resource -> Optional.ofNullable(mappings.get(resource.getId()))
                        .map(per -> resource.hasAction(per.getActions()))
                        .orElse(false));
    }
}
