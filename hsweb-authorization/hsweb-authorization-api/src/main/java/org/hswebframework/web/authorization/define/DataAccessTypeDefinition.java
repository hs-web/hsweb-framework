package org.hswebframework.web.authorization.define;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.authorization.access.DataAccessController;
import org.hswebframework.web.authorization.access.DataAccessType;
import org.hswebframework.web.bean.FastBeanCopier;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class DataAccessTypeDefinition implements DataAccessType {
    private String id;

    private String name;

    private String description;

    private Class<? extends DataAccessController> controller;

    public DataAccessTypeDefinition copy(){
        return FastBeanCopier.copy(this,DataAccessTypeDefinition::new);
    }
}
