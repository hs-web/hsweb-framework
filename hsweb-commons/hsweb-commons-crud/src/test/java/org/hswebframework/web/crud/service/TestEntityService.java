package org.hswebframework.web.crud.service;

import org.hswebframework.web.crud.entity.TestEntity;
import org.hswebframework.web.crud.events.EntityCreatedEvent;
import org.hswebframework.web.id.IDGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class TestEntityService extends GenericReactiveCrudService<TestEntity,String> {


    @EventListener
    public void handleEvent(EntityCreatedEvent<TestEntity> event){

        System.out.println(event.getEntity());
    }
}
