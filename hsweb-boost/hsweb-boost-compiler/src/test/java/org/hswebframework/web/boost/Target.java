package org.hswebframework.web.boost;

import lombok.Data;

@Data
public class Target {
    private String name;
    private int   age;
    private String[] ids;
    private NestObject nestObject;

}