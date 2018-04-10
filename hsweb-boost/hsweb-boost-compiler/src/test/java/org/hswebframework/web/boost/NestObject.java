package org.hswebframework.web.boost;

import lombok.Data;

/**
 * @author zhouhao
 * @since
 */
@Data
public class NestObject implements Cloneable {
    private String name;

    private int age;


    @Override
    public NestObject clone() throws CloneNotSupportedException {
        return (NestObject)super.clone();
    }
}
