package org.hswebframework.web.workflow.flowable.entity;

import org.activiti.engine.repository.ProcessDefinition;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class SimpleProcessDefinition implements ProcessDefinition {

    private String id;

    private String  category;
    private String  name;
    private String  key;
    private String  description;
    private int     version;
    private String  resourceName;
    private String  deploymentId;
    private String  diagramResourceName;
    private boolean suspended;

    private boolean hasStartFormKey;
    private boolean hasGraphicalNotation;
    private String  tenantId;

    public SimpleProcessDefinition() {
    }

    public SimpleProcessDefinition(ProcessDefinition definition) {
        id = definition.getId();
        category = definition.getCategory();
        name = definition.getName();
        key = definition.getKey();
        description = definition.getDescription();
        version = definition.getVersion();

        resourceName = definition.getResourceName();
        deploymentId = definition.getDeploymentId();
        diagramResourceName = definition.getResourceName();

        suspended = definition.isSuspended();

        hasGraphicalNotation = definition.hasGraphicalNotation();
        hasStartFormKey = definition.hasStartFormKey();
        tenantId = definition.getTenantId();

    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    @Override
    public boolean hasGraphicalNotation() {
        return hasGraphicalNotation;
    }

    @Override
    public boolean hasStartFormKey() {
        return hasStartFormKey;
    }

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    @Override
    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    @Override
    public String getDiagramResourceName() {
        return diagramResourceName;
    }

    public void setDiagramResourceName(String diagramResourceName) {
        this.diagramResourceName = diagramResourceName;
    }

    public boolean isHasStartFormKey() {
        return hasStartFormKey;
    }

    public void setHasStartFormKey(boolean hasStartFormKey) {
        this.hasStartFormKey = hasStartFormKey;
    }

    public boolean isHasGraphicalNotation() {
        return hasGraphicalNotation;
    }

    public void setHasGraphicalNotation(boolean hasGraphicalNotation) {
        this.hasGraphicalNotation = hasGraphicalNotation;
    }

    @Override
    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}
