package org.hswebframework.web.authorization.define;

import java.util.List;
import java.util.Set;


public class MergedAuthorizeDefinition {

    private ResourcesDefinition resources = new ResourcesDefinition();

    private DimensionsDefinition dimensions = new DimensionsDefinition();

    public Set<ResourceDefinition> getResources() {
        return resources.getResources();
    }

    public Set<DimensionDefinition> getDimensions() {
        return dimensions.getDimensions();
    }

    public void addResource(ResourceDefinition resource) {
        resources.addResource(resource, true);
    }

    public void addDimension(DimensionDefinition resource) {
        dimensions.addDimension(resource);
    }

    public void merge(List<AuthorizeDefinition> definitions) {
        for (AuthorizeDefinition definition : definitions) {
            definition.getResources().getResources().forEach(this::addResource);
            definition.getDimensions().getDimensions().forEach(this::addDimension);
        }
    }
}
