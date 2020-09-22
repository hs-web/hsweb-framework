package org.hswebframework.web.system.authorization.api.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.hswebframework.web.api.crud.entity.Entity;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "action")
public class ActionEntity implements Entity {

    @Schema(description = "操作标识,如: add,query")
    private String action;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "说明")
    private String describe;

    @Schema(description = "其他配置")
    private Map<String,Object> properties;
}
