package org.hswebframework.web.organizational.authorization.relation;

import java.io.Serializable;

/**
 *
 * @author zhouhao
 */
public class SimpleRelation implements Relation {
    private static final long serialVersionUID = 1_0;
    private String type;

    private String relation;

    private String target;

    private Serializable targetObject;

    private String name;

    private Direction direction;

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    @Override
    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public Serializable getTargetObject() {
        return targetObject;
    }

    public void setTargetObject(Serializable targetObject) {
        this.targetObject = targetObject;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }
}
