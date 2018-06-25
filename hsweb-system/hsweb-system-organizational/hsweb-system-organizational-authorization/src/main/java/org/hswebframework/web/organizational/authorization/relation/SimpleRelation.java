package org.hswebframework.web.organizational.authorization.relation;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhouhao
 */
@Data
public class SimpleRelation implements Relation {
    private static final long serialVersionUID = 1_0;

    private String dimension;

    private String relation;

    private String target;

    private Serializable targetObject;

    private String name;

    private Direction direction;

}
