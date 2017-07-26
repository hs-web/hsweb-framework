package org.hswebframework.web.organizational.authorization.relation;

import java.io.Serializable;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public interface Relation extends Serializable {
    String getType();

    String getRelation();

    String getTarget();

    String getName();

    Object getTargetObject();

    Direction getDirection();

    default boolean matchDirection(Direction direction) {
        if (getDirection() == Direction.ALL) return true;
        return getDirection() == direction;
    }

    enum Direction {
        POSITIVE, REVERSE, ALL;

        public static Direction fromString(String direction) {
            if (direction == null) return null;
            for (Direction dir : values()) {
                if (dir.toString().startsWith(direction.toUpperCase()))
                    return dir;
            }
            return null;
        }
    }

}
