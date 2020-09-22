package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hswebframework.web.api.crud.entity.Entity;

import java.util.Map;
import java.util.Set;

@Data
public class ParentPermission implements Entity {

    private static final long serialVersionUID = -7099575758680437572L;

    @Schema(description = "关联限标识")
    private String permission;

    @Schema(description = "前置操作")
    private Set<String> preActions;

    @Schema(description = "关联操作")
    private Set<String> actions;

    @Schema(description = "其他配置")
    private Map<String, Object> properties;

}
