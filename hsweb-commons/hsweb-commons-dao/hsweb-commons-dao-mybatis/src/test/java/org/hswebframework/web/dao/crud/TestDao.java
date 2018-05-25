package org.hswebframework.web.dao.crud;

import org.hswebframework.web.commons.entity.Entity;
import org.hswebframework.web.dao.CrudDao;

import java.util.List;

public interface TestDao extends CrudDao<TestEntity, Long> {
    List<TestEntity> queryNest(Entity queryEntity);

    int countNest(Entity queryEntity);

}
