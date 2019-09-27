package org.hswebframework.web.system.authorization.entity;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.crud.entity.Entity;

import java.util.Map;

@Data
public class OptionalField implements Entity {
    private String name;

    private String describe;

    private Map<String, Object> properties;
}
