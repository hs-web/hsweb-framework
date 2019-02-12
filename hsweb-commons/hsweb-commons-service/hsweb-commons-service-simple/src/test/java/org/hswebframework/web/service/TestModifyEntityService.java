package org.hswebframework.web.service;

import org.hswebframework.web.commons.entity.RecordModifierEntity;
import org.hswebframework.web.commons.entity.events.EntityModifyEvent;
import org.hswebframework.web.dao.CrudDao;
import org.hswebframework.web.id.IDGenerator;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhouhao
 * @since 3.0
 */
@Service
public class TestModifyEntityService extends GenericEntityService<TestModifyEntity, String> {

    private CrudDao<TestModifyEntity, String> dao;

    public static AtomicLong eventCounter = new AtomicLong();

    @Override
    protected IDGenerator<String> getIDGenerator() {
        return IDGenerator.MD5;
    }

    @Override
    public CrudDao<TestModifyEntity, String> getDao() {
        return dao;
    }

    public void setDao(CrudDao<TestModifyEntity, String> dao) {
        this.dao = dao;
    }

    @EventListener
    public void handleEvent(EntityModifyEvent<TestModifyEntity> modifyEvent){
        eventCounter.incrementAndGet();
        System.out.println(modifyEvent);
    }

}
