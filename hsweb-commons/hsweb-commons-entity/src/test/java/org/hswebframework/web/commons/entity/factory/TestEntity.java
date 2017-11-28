package org.hswebframework.web.commons.entity.factory;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.web.commons.entity.SimpleGenericEntity;

@Getter
@Setter
public class TestEntity extends SimpleGenericEntity<String> {
    private static final long serialVersionUID = 2468328156748007412L;

    private String name;


}
