package org.hswebframework.web.system.authorization.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class DataAccessEntity {

    private Set<String> actions;

    private String type;

    private String describe;

    private Map<String,Object> config;
}
