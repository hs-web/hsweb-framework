package org.hswebframework.web.authorization.basic.define;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.define.AuthorizeDefinition;
import org.hswebframework.web.authorization.define.DimensionsDefinition;
import org.hswebframework.web.authorization.define.ResourcesDefinition;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
public class MergedAuthorizeDefinition implements Serializable {

    private ResourcesDefinition resources = new ResourcesDefinition();
    private DimensionsDefinition dimensions = new DimensionsDefinition();



}
