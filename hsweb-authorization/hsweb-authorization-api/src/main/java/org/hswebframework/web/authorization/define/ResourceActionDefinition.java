package org.hswebframework.web.authorization.define;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResourceActionDefinition {
    private String id;

    private String name;

    private String description;

    private DataAccessDefinition dataAccess = new DataAccessDefinition();
}
