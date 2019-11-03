package org.hswebframework.web.system.authorization.api.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.api.crud.entity.Entity;

import java.util.Map;

@Data
@EqualsAndHashCode(of = "name")
public class OptionalField implements Entity {
    private String name;

    private String describe;

    private Map<String, Object> properties;
}
