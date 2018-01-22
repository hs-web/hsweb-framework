package org.hswebframework.web.service;

import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;

/**
 * @author zhouhao
 * @since 3.0
 */
public class TestEntityService extends GenericEntityService<TestEntity, String> {

    private CrudDao<TestEntity, String> dao;

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CrudDao<TestEntity, String> getDao() {
        return dao;
    }

    public void setDao(CrudDao<TestEntity, String> dao) {
        this.dao = dao;
    }
}
