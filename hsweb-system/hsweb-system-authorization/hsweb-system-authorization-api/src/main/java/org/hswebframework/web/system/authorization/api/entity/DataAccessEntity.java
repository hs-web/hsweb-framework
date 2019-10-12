package org.hswebframework.web.system.authorization.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
public class DataAccessEntity {

    private String action;

    private String type;

    private String describe;

    private Map<String,Object> config;
}
