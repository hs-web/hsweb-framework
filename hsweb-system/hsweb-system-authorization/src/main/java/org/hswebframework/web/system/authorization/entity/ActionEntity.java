package org.hswebframework.web.system.authorization.entity;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.crud.entity.Entity;

import java.util.Map;

@Getter
@Setter
public class ActionEntity implements Entity {

    private String action;

    private String describe;

    private Map<String,Object> properties;
}
