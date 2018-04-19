package org.hswebframework.web.bean;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author zhouhao
 * @since
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NestObject implements Cloneable {
    private String name;

    private int age;


    @Override
    public NestObject clone() throws CloneNotSupportedException {
        return (NestObject)super.clone();
    }
}
