package org.hswebframework.web.authorization.define;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.bean.FastBeanCopier;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class ResourceActionDefinition {
    private String id;

    private String name;

    private String description;

    private DataAccessDefinition dataAccess = new DataAccessDefinition();

    public ResourceActionDefinition copy(){
        return FastBeanCopier.copy(this,ResourceActionDefinition::new);
    }

}
