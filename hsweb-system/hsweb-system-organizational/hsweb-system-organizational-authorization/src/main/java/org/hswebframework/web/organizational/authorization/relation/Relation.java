package org.hswebframework.web.organizational.authorization.relation;

import java.io.Serializable;

/**
 * 关系，用于获取人员等关系信息
 *
 * @author zhouhao
 * @see Relations
 * @since 3.0
 */
public interface Relation extends Serializable {

    /**
     * 默认类型:机构
     */
    String TYPE_ORG = "org";

    /**
     * 默认类型:部门
     */
    String TYPE_DEPARTMENT = "department";

    /**
     * 默认类型:岗位
     */
    String TYPE_POSITION = "position";

    /**
     * 默认类型:人员
     */
    String TYPE_PERSON = "person";

    /**
     * @return 关系类型，如:person,department
     */
    String getType();

    /**
     * @return 关系，如: leader,member
     */
    String getRelation();

    /**
     * @return 关系目标表识（和谁建立关系），通常为目标的id
     */
    String getTarget();

    /**
     * @return 关系目标对象，用于获取建立关系对象完整信息，返回值的类型可能随着{@link this#getType()}的不同而变化
     */
    Object getTargetObject();

    /**
     * @return 关系名称，与{@link this#getType()} 对应，如: 经理,员工
     */
    String getName();

    /**
     * @return 关系的方向
     * @see Direction
     */
    Direction getDirection();

    /**
     * 匹配方向，如果当前的方向为ALl，则全部返回true
     * <pre>
     *     direction=ALL;
     *     matchDirection(POSITIVE) -> true
     *     matchDirection(REVERSE) -> true
     *     matchDirection(ALL) -> true
     * </pre>
     * <p>
     * <pre>
     *     direction=POSITIVE;
     *     matchDirection(POSITIVE) -> true
     *     matchDirection(REVERSE) -> false
     *     matchDirection(ALL) -> false
     * </pre>
     *
     * @param direction 要匹配的方向枚举
     * @return 匹配结果
     */
    default boolean matchDirection(Direction direction) {
        return getDirection() == Direction.ALL || getDirection() == direction;
    }

    /**
     * 匹配方向，如果当前的方向为ALl，则全部返回true
     * <pre>
     *     direction=ALL;
     *     matchDirection("A") -> true
     *     matchDirection("ALL") -> true
     *     matchDirection("R") -> true
     *     matchDirection("P") -> true
     *     matchDirection("O") -> false
     * </pre>
     * <p>
     * <pre>
     *     direction=POSITIVE;
     *     matchDirection("P") -> true
     *     matchDirection("POS") -> true
     *     matchDirection("A") -> false
     *     matchDirection("O") -> false
     * </pre>
     *
     * @param direction 要匹配的方向字符
     * @return 匹配结果
     * @see Direction#fromString(String)
     */
    default boolean matchDirection(String direction) {
        return matchDirection(Direction.fromString(direction));
    }

    /**
     * 关系方向,例如，我和张三建立关系，POSITIVE：我是张三的经理 ，REVERSE张三是我的经理
     *
     * @author zhouhao
     * @since 3.0
     */
    enum Direction {
        /**
         * 正向关系
         */
        POSITIVE,
        /**
         * 反向关系
         */
        REVERSE,
        /**
         * 双向关系
         */
        ALL;

        public static Direction fromString(String direction) {
            if (direction == null) return null;
            for (Direction dir : values()) {
                //以名称开头则认为是同一个方向
                if (dir.name().startsWith(direction.toUpperCase()))
                    return dir;
            }
            return null;
        }
    }

}
