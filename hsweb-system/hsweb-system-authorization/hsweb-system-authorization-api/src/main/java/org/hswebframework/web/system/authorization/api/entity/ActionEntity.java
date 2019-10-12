package org.hswebframework.web.system.authorization.api.entity;

import lombok.*;
import org.hswebframework.web.api.crud.entity.Entity;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActionEntity implements Entity {

    private String action;

    private String describe;

    private Map<String,Object> properties;
}
