package org.hswebframework.web.system.authorization.api.entity;

import lombok.Data;
import org.hswebframework.web.api.crud.entity.Entity;

import java.util.Map;
import java.util.Set;

@Data
public class ParentPermission implements Entity {

    private static final long serialVersionUID = -7099575758680437572L;

    private String permission;

    private Set<String> preActions;

    private Set<String> actions;

    private Map<String, Object> properties;

}
