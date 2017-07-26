package org.hswebframework.web.organizational.authorization.relation;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 人员关系信息
 *
 * @author zhouhao
 * @since 3.0
 */
public interface Relations extends Serializable {

    /**
     * 判断人员与目标是否存在某个关系
     * <pre>
     *     //判断是否是人员:张三的leader关系
     *     relations.has("leader","person","张三",POSITIVE);
     * </pre>
     * <pre>
     *     //判断人员:张三是否是本人的leader关系
     *     relations.has("leader","person","张三",REVERSE);
     * </pre>
     * <pre>
     *     //判断人员:张三和本人是否相互为friend关系
     *     relations.has("friend","person","张三",ALL);
     * </pre>
     *
     * @param relation 关系
     * @param type     关系类型
     * @param to       目标
     * @return 是否存在关系
     */
    default boolean has(String relation, String type, String to, Relation.Direction direction) {
        return getAllRelations().stream().anyMatch(rel ->
                rel.getRelation().equals(relation)
                        && rel.getType().equals(type)
                        && rel.getTarget().equals(to)
                        && rel.matchDirection(direction));
    }

    /**
     * @see this#has(String, String, String, Relation.Direction)
     */
    default boolean has(String relation, String type, String target, String direction) {
        return has(relation, type, target, Relation.Direction.fromString(direction));
    }

    /**
     * use {@link Relation.Direction#POSITIVE}
     *
     * @see this#has(String, String, String, Relation.Direction)
     */
    default boolean has(String relation, String type, String target) {
        return has(relation, type, target, Relation.Direction.POSITIVE);
    }

    
    default List<Relation> getRelations(String relation, String type) {
        return getAllRelations().stream()
                .filter(rel -> rel.getRelation().equals(relation) && rel.getType().equals(type))
                .collect(Collectors.toList());
    }

    default List<Relation> getRelations(String relation, Relation.Direction direction) {
        return getAllRelations().stream()
                .filter(rel -> rel.getRelation().equals(relation) && rel.matchDirection(direction))
                .collect(Collectors.toList());
    }

    List<Relation> getAllRelations();
}
