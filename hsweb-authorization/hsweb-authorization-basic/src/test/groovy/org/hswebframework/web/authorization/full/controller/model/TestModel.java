package org.hswebframework.web.authorization.full.controller.model;

import lombok.Data;
import org.hswebframework.web.commons.model.Model;

/**
 * @author zhouhao
 * @since 3.0.2
 */
@Data
public class TestModel implements Model {
    private String id;

    private String name;

    private int age;

    private String orgId;

    private String password;
}
