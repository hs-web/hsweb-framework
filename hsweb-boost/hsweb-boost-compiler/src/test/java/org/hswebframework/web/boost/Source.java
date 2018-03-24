package org.hswebframework.web.boost;

import lombok.Data;

@Data
public class Source {
    private String name;
    private int    age;
    private String[] ids;

    private NestObject nestObject;

}


