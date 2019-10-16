package org.hswebframework.web.authorization.define;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.access.DataAccessType;

@Getter
@Setter
public class DataAccessTypeDefinition implements DataAccessType {
    private String id;

    private String name;

    private String description;

    private Class<? extends DataAccessController> controller;
}
