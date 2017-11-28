package org.hswebframework.web.commons.entity.factory;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class MapperEntityFactoryTests {


    @Test
    public void testCreateEntity() {
        MapperEntityFactory entityFactory = new MapperEntityFactory();


        entityFactory.addMapping(TestEntity.class, entityFactory.initCache(NewTestEntity.class));

        TestEntity entity = entityFactory.newInstance(TestEntity.class);

        Assert.assertEquals(entity.getClass(), NewTestEntity.class);


        entity = entityFactory.copyProperties(new HashMap<String, Object>() {
            private static final long serialVersionUID = 6458422824954290386L;

            {
                put("name", "张三");
                put("nickName", "小张");
            }
        }, entity);

        Assert.assertEquals(entity.getName(), "张三");
        Assert.assertEquals(((NewTestEntity) entity).getNickName(), "小张");


        entityFactory.addCopier(new CustomPropertyCopier());

        HashMap<String, Object> data = new HashMap<>();
        data.put("name", "李四");
        data.put("nickName", "小李");
        entityFactory.copyProperties(data, entity);

        Assert.assertEquals(entity.getName(), "李四");
        Assert.assertEquals(((NewTestEntity) entity).getNickName(), "小李");

    }

    class CustomPropertyCopier implements PropertyCopier<HashMap, NewTestEntity> {

        @Override
        public NewTestEntity copyProperties(HashMap source, NewTestEntity target) {
            target.setName((String) source.get("name"));
            target.setNickName((String) source.get("nickName"));
            return target;
        }
    }
}