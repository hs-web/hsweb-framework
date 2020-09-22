package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.Entity;

import java.util.Map;

@Data
@EqualsAndHashCode(of = "name")
public class OptionalField implements Entity {

    @Schema(description = "字段名")
    private String name;

    @Schema(description = "说明")
    private String describe;

    @Schema(description = "其他配置")
    private Map<String, Object> properties;
}
