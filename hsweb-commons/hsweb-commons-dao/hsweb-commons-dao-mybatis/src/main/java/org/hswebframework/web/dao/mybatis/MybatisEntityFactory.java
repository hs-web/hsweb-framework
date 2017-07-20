package org.hswebframework.web.dao.mybatis;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.hswebframework.web.commons.entity.factory.EntityFactory;

import java.util.*;

/**
 * TODO 完成注释
 *
 * @author zhouhao
 */
public class MybatisEntityFactory extends DefaultObjectFactory {

    private EntityFactory entityFactory;

    public MybatisEntityFactory(EntityFactory entityFactory) {
        this.entityFactory = entityFactory;
    }

    @Override
    protected Class<?> resolveInterface(Class<?> type) {
        Class<?> classToCreate;
        if (type == List.class || type == Collection.class || type == Iterable.class) {
            classToCreate = ArrayList.class;
        } else if (type == Map.class) {
            classToCreate = HashMap.class;
        } else if (type == SortedSet.class) { // issue #510 Collections Support
            classToCreate = TreeSet.class;
        } else if (type == Set.class) {
            classToCreate = HashSet.class;
        } else {
            // entity interface
            classToCreate = entityFactory.getInstanceType(type);
        }
        return classToCreate;
    }
}
